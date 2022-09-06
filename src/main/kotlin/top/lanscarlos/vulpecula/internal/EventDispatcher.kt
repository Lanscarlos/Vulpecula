package top.lanscarlos.vulpecula.internal

import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.*
import taboolib.common5.Baffle
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.kether.Script
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.internal.EventListener.Companion.getListener
import top.lanscarlos.vulpecula.utils.*
import top.lanscarlos.vulpecula.utils.Debug.debug
import java.io.File
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
    config: ConfigurationSection
) {

    val eventName = config.getString("listen")?.let {
        EventMapping.mapping(it) ?: error("Cannot get Dispatcher \"$id\" 's listen event mapping: \"$it\"")
    } ?: error("Dispatcher \"$id\" 's listen event is undefined!")

    val priority = config.getString("priority")?.let {
        EventPriority.valueOf(it.uppercase())
    } ?: EventPriority.NORMAL

    val ignoreCancelled = config.getBoolean("ignore-cancelled", true)
    private val preHandle = config["pre-handle"]?.formatToScript()
    private val postHandle = config["post-handle"]?.formatToScript()
    private val baffle = config.getConfigurationSection("baffle")?.let { initBaffle(it) }
    private val variables = config.getConfigurationSection("variables")?.let { initVariables(it) }

    private val handlerCache = mutableSetOf<String>()
    private val handlers = mutableSetOf<String>()

    private lateinit var namespace: List<String>
    private lateinit var scriptSource: String
    private lateinit var script: Script

    fun run(event: Event) {

        if (!::script.isInitialized) return

        val player = when (event) {
            is PlayerEvent -> event.player
            is BlockBreakEvent -> event.player
            is BlockPlaceEvent -> event.player
            is EntityDamageEvent -> (event.entity as? Player)
            is EntityDamageByEntityEvent -> {
                when (event.damager) {
                    is Player -> event.damager
                    is Projectile -> ((event.damager as Projectile).shooter as? Player)
                    else -> null
                }
            }
            is InventoryClickEvent -> event.whoClicked as? Player
            else -> null
        }

        if (baffle != null) {
            val key = player?.name ?: event.eventName
            if (!baffle.hasNext(key)) return
        }

        debug(Debug.HIGHEST, "调度器 $id 正在运行...")
//        debug(Debug.HIGHEST, script)

        // 执行脚本
        script.runActions {
            set("@Event", event)
            player?.let {
                set("@Sender", it)
                set("player", it)
            }
        }
    }

    fun buildScriptSource() {

        namespace = handlers.toMutableList().also {
            if (handlerCache.isNotEmpty()) {
                it.addAll(handlerCache)
            }
        }.mapNotNull {
            EventHandler.get(it)
        }.flatMap { it.namespace }.distinct()

        val script = StringBuilder("def main = {\n")
        preHandle?.let { script.append("$it\n") }
        variables?.forEach { (key, value) ->
            script.append("set $key to $value\n")
        }

        val sorted = handlers.toMutableList().also {
            if (handlerCache.isNotEmpty()) {
                it.addAll(handlerCache)
            }
        }.mapNotNull {
            EventHandler.get(it)
        }.sortedByDescending { it.priority }

        sorted.forEach {
            script.append("call handler_${it.hash}\n")
        }
        postHandle?.let { script.append("\n$it\n") }
        script.append("}\n\n")
        sorted.forEach {
            script.append(it.script)
        }

        scriptSource = script.toString()
    }

    /**
     * 通过脚本源码构建脚本对象
     * */
    fun buildScript() {

        if (!::scriptSource.isInitialized) {
            // 构建脚本源码
            buildScriptSource()
        }

        debug(Debug.HIGHEST, "构建脚本：\n$scriptSource")

        // 脚本安全性检测
        try {

            this.script = scriptSource.parseToScript(namespace)

            // 检测通过，将缓存导入
            if (handlerCache.isNotEmpty()) {
                handlers.addAll(handlerCache)
                handlerCache.clear()
            }

        } catch (e: Exception) {

            // 检测 Dispatcher
            if (!compileChecking()) {
                return
            }

            val sorted = handlers.toMutableList().also {
                if (handlerCache.isNotEmpty()) {
                    it.addAll(handlerCache)
                }
            }.mapNotNull {
                EventHandler.get(it)
            }.sortedByDescending { it.priority }

            // 检测 Handler
            for (handler in sorted) {
                if (!handler.compileChecking()) return
            }

//            warning(e.localizedMessage)
            console().sendLang("Dispatcher-Load-Failed-Details", id, "UNCHECK_PART", e.localizedMessage)
        }

    }

    fun compileChecking(): Boolean {
        var pointer = "Uninitialized"
        return try {

            pointer = "pre-handle"
            preHandle?.parseToScript(namespace)

            variables?.forEach { (key, source) ->
                pointer = "variables.$key"
                source.parseToScript(namespace)
            }

            pointer = "post-handle"
            postHandle?.parseToScript(namespace)

            true
        } catch (e: Exception) {
            console().sendLang("Dispatcher-Load-Failed-Details", id, pointer, e.localizedMessage)
            false
        }
    }

    fun postLoad() {
        buildScriptSource()
        buildScript()
    }

    fun releaseBaffle(player: Player? = null) {
        if (baffle != null) {
            if (player != null) {
                baffle.reset(player.name)
            } else {
                baffle.resetAll()
            }
        }
    }

    fun addHandler(handler: EventHandler, replace: Boolean = false) {
        if (handler.id in handlers || handler.id in handlerCache) {
            // 已存在相同 id 的 Handler
            if (!replace) return
            handlers.remove(handler.id)
        }
        handlerCache += handler.id
    }

    fun removeHandler(id: String) {
        handlerCache.remove(id)
    }

    private fun initVariables(config: ConfigurationSection): Map<String, String> {
        return config.getKeys(false).mapNotNull { key ->
            config.getString(key)?.let { key to it }
        }.toMap()
    }

    private fun initBaffle(config: ConfigurationSection): Baffle? {
        return when (val it = config.getString("type")) {
            "time" -> {
                val time = config.getInt("time", -1)
                if (time > 0) {
                    Baffle.of(time * 50L, TimeUnit.MILLISECONDS)
                } else {
                    warning("Illegal baffle time \"$time\" at EventDispatcher \"$id\"!")
                    null
                }
            }
            "count" -> {
                val count = config.getInt("count", -1)
                if (count > 0) {
                    Baffle.of(count)
                } else {
                    warning("Illegal baffle count \"$count\" at EventDispatcher \"$id\"!")
                    null
                }
            }
            else -> {
                if (it != null) {
                    warning("Unknown baffle type \"$it\" at EventDispatcher \"$id\"!")
                }
                null
            }
        }
    }

    override fun toString(): String {
        return "EventDispatcher(id='$id')"
    }

    companion object {

        private val folder by lazy {
            File(getDataFolder(), "dispatchers")
        }

        private val fileCache = mutableMapOf<File, MutableSet<String>>()
        private val cache = mutableMapOf<String, EventDispatcher>()

        fun get(id: String): EventDispatcher? = cache[id]

        private fun onFileChanged(file: File) {
            val start = timing()
            try {

                // 获取已存在的 Dispatcher
                val existing = fileCache.remove(file)?.mapNotNull {
                    cache.remove(it)
                }?.associateBy { it.id }?.toMutableMap()

                // 加载新的 Handler
                val loaded = loadFromFile(file)

                // 遍历新对象
                for (dispatcher in loaded) {

                    if (dispatcher.id in cache) {
                        // id 冲突
                        val conflict = fileCache.filter { dispatcher.id in it.value }.firstNotNullOfOrNull { it.key }
                        console().sendLang("Dispatcher-Load-Failed-Conflict", dispatcher.id, file.canonicalPath, conflict?.canonicalPath ?: "UNKNOWN_FILE")
                        continue
                    }

                    // 获取对应的监听器对象
                    val listener = dispatcher.getListener() ?: continue

                    // 尝试获取旧对象
                    val old = existing?.remove(dispatcher.id)

                    if (old != null) {

                        // 载入旧对象的所有 Handler
                        dispatcher.handlerCache += old.handlers
                        dispatcher.handlerCache += old.handlerCache

                        // 比对新旧对象的监听器/事件要素
                        if (listener.id != old.getListener()?.id) {
                            // 新对象发生改变，更改监听器
                            old.getListener()?.removeDispatcher(old.id)
                        }

                        // 构建脚本源码
                        dispatcher.buildScriptSource()

                        // 比对新旧对象的脚本源码
                        if (dispatcher.scriptSource != old.scriptSource) {
                            // 脚本源码不一致，重构脚本
                            dispatcher.buildScript()
                        }
                    } else {
                        // 不存在旧对象

                        // 获取与该 Dispatcher 绑定的 Handler
                        val handlers = EventHandler.getAll().filter { dispatcher.id in it.binding }.map { it.id }
                        dispatcher.handlerCache += handlers
                    }

                    // 尝试注册监听器
                    listener.addDispatcher(dispatcher, true)
                    listener.register()

                    // 存入缓存
                    cache[dispatcher.id] = dispatcher
                    // 记录文件映射信息
                    fileCache.computeIfAbsent(file) { mutableSetOf() } += dispatcher.id

                }

                // 遍历剩余旧对象
                existing?.values?.forEach { dispatcher ->
                    dispatcher.getListener()?.removeDispatcher(dispatcher.id)
                }

                console().sendLang("Dispatcher-Load-Automatic-Succeeded", file.name, loaded.size, timing(start))
            } catch (e: Exception) {
                console().sendLang("Dispatcher-Load-Automatic-Failed", file.name, e.localizedMessage, timing(start))
            }
        }

        private fun loadFromFile(file: File): Set<EventDispatcher> {
            val loaded = mutableSetOf<EventDispatcher>()
            file.toConfig().forEachSections { key, section ->
                loaded += EventDispatcher(key, section)
            }
            return loaded
        }

        fun load(): String {
            val start = timing()
            return try {

                // 移除文件监听器
                fileCache.keys.forEach { it.removeWatcher() }

                // 清除缓存
                cache.clear()
                fileCache.clear()

                val mapping = mutableMapOf<String, File>()

                folder.ifNotExists {
                    listOf(
                        "def.yml"
                    ).forEach { releaseResourceFile("dispatchers/$it", true) }
                }.getFiles().forEach { file ->

                    loadFromFile(file).forEach inner@{ dispatcher ->
                        if (dispatcher.id in mapping) {
                            // id 冲突
                            val conflict = mapping[dispatcher.id]!!
                            console().sendLang("Dispatcher-Load-Failed-Conflict", dispatcher.id, conflict.canonicalPath, file.canonicalPath)
                            return@inner
                        }

                        // 生成监听器
                        dispatcher.getListener()?.addDispatcher(dispatcher)

                        // 记录临时映射信息
                        mapping[dispatcher.id] = file

                        // 载入缓存
                        cache[dispatcher.id] = dispatcher
                        // 记录文件映射信息
                        fileCache.computeIfAbsent(file) { mutableSetOf() } += dispatcher.id
                    }

                    // 添加文件监听器
                    file.addWatcher(false) { onFileChanged(this) }
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

            // 绑定 Dispatcher
            EventHandler.getAll().forEach { handler ->
                handler.binding.mapNotNull {
                    get(it)
                }.forEach { dispatcher ->
                    dispatcher.addHandler(handler)
                }
            }

            cache.values.forEach {
                it.postLoad()
            }
        }
    }
}