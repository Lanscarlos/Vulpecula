package top.lanscarlos.vulpecula.internal

import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.event.player.PlayerEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.*
import taboolib.common5.Baffle
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.Quest
import taboolib.module.kether.Script
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.config.VulConfig
import top.lanscarlos.vulpecula.script.ScriptCompiler
import top.lanscarlos.vulpecula.utils.*
import top.lanscarlos.vulpecula.utils.Debug.debug
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-09-03 15:11
 */
class EventDispatcher(
    val id: String,
    val path: String,
    val wrapper: VulConfig
) : ScriptCompiler {

    val eventName by wrapper.read("listen") { name ->
        name?.toString() ?: error("Dispatcher \"$id\" 's listen event is undefined!")
    }

    val priority by wrapper.read("priority") {
        EventPriority.values().firstOrNull { priority ->
            priority.name.equals(it?.toString(), true)
        } ?: EventPriority.NORMAL
    }

    val ignoreCancelled by wrapper.readBoolean("ignore-cancelled", true)

    val namespace by wrapper.readStringList("namespace")

    val preHandle by wrapper.read("pre-handle") {
        if (it != null) buildSection(it) else StringBuilder()
    }

    val postHandle by wrapper.read("post-handle") {
        if (it != null) buildSection(it) else StringBuilder()
    }

    val exception by wrapper.read("exception") {
        if (it != null) buildException(it) else emptyList()
    }

    val variables by wrapper.read("variables") { value ->
        buildVariables(value, mapOf())
    }

    val baffle by wrapper.read("baffle") { value ->
        if (value == null) return@read null

        val section = if (value is ConfigurationSection) {
            value.toMap()
        } else value as Map<*, *>

        when {
            "time" in section -> {
                // 按时间阻断
                val time = section["time"].coerceInt(-1)
                if (time > 0) {
                    Baffle.of(time * 50L, TimeUnit.MILLISECONDS)
                } else {
                    warning("Illegal baffle time \"$time\" at EventDispatcher \"$id\"!")
                    null
                }
            }
            "count" in section -> {
                // 按次数阻断
                val count = section["count"].coerceInt(-1)
                if (count > 0) {
                    Baffle.of(count)
                } else {
                    warning("Illegal baffle count \"$count\" at EventDispatcher \"$id\"!")
                    null
                }
            }
            else -> null
        }
    }

    val handlers = mutableListOf<EventHandler>()

    /* 含主方法的方法体 */
    lateinit var source: StringBuilder

    /* 包含主方法的可执行脚本 */
    lateinit var script: Script

    fun registerListener() {
        // 注销将先前的事件任务
        unregisterListener()

        EventListener.registerTask(eventName, priority, ignoreCancelled, "dispatcher-$id") { event ->
            call(event)
        }
    }

    fun unregisterListener() {
        EventListener.unregisterTask("dispatcher-$id")
    }

    override fun buildSource(): StringBuilder {
        val builder = StringBuilder()

        /*
        * 构建前置变量
        * */
        if (variables.isNotEmpty()) {
            compileVariables(builder, variables)
            builder.append('\n')
        }

        /*
        * 构建预处理语句
        * */
        if (preHandle.isNotEmpty()) {
            builder.append(preHandle.toString())
            builder.append("\n\n")
        }

        /*
        * 构建调度语句
        * call $handler_<hash>
        * */
        if (handlers.isNotEmpty()) {
            // 根据处理器优先级升序排序，优先级越高越先被执行
            handlers.sortByDescending { it.priority }

            handlers.forEach {
                builder.append("call ${it.hashName}\n")
            }
            builder.append('\n')
        }

        /*
        * 构建尾处理语句
        * */
        if (postHandle.isNotEmpty()) {
            builder.append(postHandle.toString())
            builder.append('\n')
        }

        /*
        * 构建异常处理
        * try {
        *   ...$content
        * } catch with "..." {
        *   ...
        * }
        * */
        if (exception.isNotEmpty() && (exception.size > 1 || exception.first().second.isNotEmpty())) {
            // 提取先前所有内容
            val content = builder.extract()

            builder.append("try {\n")
            builder.append(content)
            builder.append("\n}")

            for (it in exception) {
                if (it.second.isEmpty()) continue

                builder.append(" catch")
                if (it.first.isNotEmpty()) {
                    builder.append(" with \"")
                    builder.append(it.first.joinToString(separator = "|"))
                    builder.append("\" ")
                }
                builder.append("{\n")
                builder.appendWithIndent(it.second.toString(), suffix = "\n")
                builder.append("}")
            }
        }

        /*
        * 收尾
        * 构建主方法
        * def main = {
        *   ...$content
        * }
        * */

        // 提取先前所有内容
        val content = builder.extract()

        // 构建方法体
        builder.append("def main = {\n")
        builder.appendWithIndent(content, suffix = "\n")
        builder.append("}")

        return builder
    }

    override fun compileScript() {
        try {
            // 尝试构建脚本
            val source = buildSource()
            val quest = source.toString().compileKetherScript(namespace)


            // 编译通过
            this.source = source
            script = DispatcherQuest(this, quest.blocks)

            debug(Debug.HIGHEST, "dispatcher \"$id\" build source:\n$source")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun call(event: Event) {

        if (!::script.isInitialized) {
            warning("Script of Dispatcher \"$id\" has built failed. Please check config!")
            return
        }

        val player = when (event) {
            is PlayerEvent -> event.player
            is BlockBreakEvent -> event.player
            is BlockPlaceEvent -> event.player
            is EntityDamageByEntityEvent -> {
                when (event.damager) {
                    is Player -> event.damager
                    is Projectile -> ((event.damager as Projectile).shooter as? Player)
                    else -> null
                }
            }
            is EntityEvent -> (event.entity as? Player)
            is InventoryClickEvent -> event.whoClicked as? Player
            is InventoryEvent -> event.view.player as? Player
            else -> null
        }

        baffle?.let { baffle ->
            val key = player?.name ?: event.eventName
            if (!baffle.hasNext(key)) return
        }

        debug(Debug.HIGHEST, "调度器 $id 正在运行...")

        // 执行脚本
        script.runActions {
            setVariable(
                "@Event", "event",
                value = event
            )
            setVariable(
                "@Sender", "@Player", "player",
                value = player
            )
        }
    }

    /**
     * 对照并尝试更新
     * */
    fun contrast(section: ConfigurationSection) {
        var relisten = false
        var recompile = false

        wrapper.updateSource(section).forEach {
            debug(Debug.MONITOR, "Dispatcher $id contrast ${it.first} to ${it.second}")
            when (it.first) {
                "listen", "priority", "ignore-cancelled" -> relisten = true
                "namespace", "pre-handle", "post-handle", "variables", "exception" -> recompile = true
            }
        }

        if (recompile) compileScript()
        if (relisten) registerListener()
    }

    fun releaseBaffle(player: Player? = null) {
        baffle?.let { baffle ->
            if (player != null) {
                baffle.reset(player.name)
            } else {
                baffle.resetAll()
            }
        }
    }

    fun addHandler(handler: EventHandler) {
        if (handler !in handlers) {
            handlers += handler
        }
    }

    fun removeHandler(handler: EventHandler) {
        handlers.remove(handler)
    }

    override fun toString(): String {
        return "EventDispatcher(id='$id')"
    }

    class DispatcherQuest(
        val dispatcher: EventDispatcher,
        val main: Map<String, Quest.Block>
    ) : Quest {

        val mapping get() = mutableMapOf<String, Quest.Block>().also { map ->
            map.putAll(main)
            map.putAll(dispatcher.handlers.flatMap { it.scriptBlocks.values }.associateBy { it.label })
        }

        override fun getId(): String {
            return dispatcher.id
        }

        override fun getBlock(label: String): Optional<Quest.Block> {
            return Optional.ofNullable(mapping[label])
        }

        override fun getBlocks(): Map<String, Quest.Block> {
            return mapping
        }

        override fun blockOf(action: ParsedAction<*>): Optional<Quest.Block> {
            return Optional.ofNullable(mapping.values.firstOrNull { action in it.actions })
        }
    }

    companion object {

        val folder = File(getDataFolder(), "dispatchers")
        val cache = mutableMapOf<String, EventDispatcher>()

        fun get(id: String): EventDispatcher? = cache[id]

        private fun onFileChanged(file: File) {
            val start = timing()
            try {

                var counter = 0
                val path = file.canonicalPath
                val config = file.toConfig()
                val keys = config.getKeys(false).toMutableSet()

                // 遍历已存在的调度器
                val iterator = cache.iterator()
                while (iterator.hasNext()) {
                    val dispatcher = iterator.next().value
                    if (dispatcher.path != path) continue

                    if (dispatcher.id in keys) {
                        // 调度器仍然存在于文件中，尝试更新调度器属性
                        config.getConfigurationSection(dispatcher.id)?.let { section ->
                            if (section.getBoolean("disable", false)) return@let null

                            debug(Debug.HIGH, "Dispatcher contrasting \"${dispatcher.id}\"")
                            dispatcher.contrast(section)
                            counter += 1
                        } ?: let {
                            // 节点寻找失败，删除调度器
                            dispatcher.unregisterListener()
                            dispatcher.handlers.forEach { it.unbind(dispatcher) }
                            dispatcher.handlers.clear()

                            iterator.remove()
                            debug(Debug.HIGH, "Dispatcher delete \"${dispatcher.id}\"")
                        }

                        // 移除该 id
                        keys -= dispatcher.id
                    } else {
                        // 该调度器已被用户删除
                        dispatcher.unregisterListener()
                        dispatcher.handlers.forEach { it.unbind(dispatcher) }
                        dispatcher.handlers.clear()

                        iterator.remove()
                        debug(Debug.HIGH, "Dispatcher delete \"${dispatcher.id}\"")
                    }
                }

                // 遍历新的调度器
                for (key in keys) {

                    // 检查 id 冲突
                    if (key in cache) {
                        val conflict = cache[key]!!
                        console().sendLang("Dispatcher-Load-Failed-Conflict", key, conflict.path, path)
                        continue
                    }

                    config.getConfigurationSection(key)?.let { section ->
                        if (section.getBoolean("disable", false)) return@let

                        val dispatcher = EventDispatcher(key, path, section.wrapper())
                        cache[key] = dispatcher

                        // 绑定 handler
                        for (handler in EventHandler.getAll()) {
                            if (dispatcher.id !in handler.binding) continue
                            handler.bind(dispatcher)
                        }

                        // 构建脚本
                        dispatcher.compileScript()

                        // 注册监听器
                        dispatcher.registerListener()

                        counter += 1
                        debug(Debug.HIGH, "Dispatcher loaded \"$key\"")
                    }
                }

                console().sendLang("Dispatcher-Load-Automatic-Succeeded", file.name, counter, timing(start))
            } catch (e: Exception) {
                console().sendLang("Dispatcher-Load-Automatic-Failed", file.name, e.localizedMessage, timing(start))
            }
        }

        fun load(): String {
            val start = timing()
            return try {

                // 清除缓存
                cache.clear()

                folder.ifNotExists {
                    listOf(
                        "def.yml"
                    ).forEach { releaseResourceFile("dispatchers/$it", true) }
                }.getFiles().forEach { file ->

                    val path = file.canonicalPath

                    // 添加文件监听器
                    file.addWatcher(false) { onFileChanged(this) }

                    // 加载文件
                    file.toConfig().forEachSection { key, section ->
                        if (section.getBoolean("disable", false)) return@forEachSection

                        // 检查 id 冲突
                        if (key in cache) {
                            val conflict = cache[key]!!
                            console().sendLang("Handler-Load-Failed-Conflict", key, conflict.path, path)
                            return@forEachSection
                        }

                        cache[key] = EventDispatcher(key, path, section.wrapper())
                        debug(Debug.HIGH, "Handler loaded \"$key\"")
                    }
                }

                console().asLangText("Dispatcher-Load-Succeeded", cache.size, timing(start)).also {
                    console().sendMessage(it)
                }
            } catch (e: Exception) {
                console().asLangText("Dispatcher-Load-Failed", e.localizedMessage, timing(start)).also {
                    console().sendMessage(it)
                }
            }
        }

        fun postLoad() {
            cache.values.forEach {
                // 编译脚本
                it.compileScript()
                // 注册事件监听器
                it.registerListener()
            }
        }
    }
}