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
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.*
import taboolib.common5.Baffle
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.kether.Script
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.parseKetherScript
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.internal.ScriptBuilder.Companion.parseToScript
import top.lanscarlos.vulpecula.utils.*
import top.lanscarlos.vulpecula.utils.Debug.debug
import top.lanscarlos.vulpecula.utils.formatToScript
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-08-19 13:41
 */
class EventDispatcher(
    val id: String,
    config: ConfigurationSection
) {

    val ignoreCancelled = config.getBoolean("ignore-cancelled", true)
    private val preHandle = config["pre-handle"]?.formatToScript()
    private val postHandle = config["post-handle"]?.formatToScript()
    private val baffle = config.getConfigurationSection("baffle")?.let { initBaffle(it) }
    private val variables = config.getConfigurationSection("variables")?.let { initVariables(it) }
    private val handlers = mutableSetOf<EventHandler>()

    private lateinit var namespace: List<String>
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
        ScriptContext.create(script).apply {
            set("@Event", event)
            player?.let {
                set("@Sender", it)
                set("player", it)
            }
        }.runActions()
    }

    fun removeHandler(handler: EventHandler) {
        handlers -= handler
//        buildScript()
    }

    fun postLoad() {
        namespace = handlers.flatMap { it.namespace }.distinct()
        buildScript()
    }

    fun release(player: Player? = null) {
        if (baffle != null) {
            if (player != null) {
                baffle.reset(player.name)
            } else {
                baffle.resetAll()
            }
        }
    }

    fun buildScript() {
        val script = StringBuilder("def main = {\n")

        preHandle?.let { script.append("$it\n") }

        variables?.forEach { (key, value) ->
            script.append("set $key to $value\n")
        }

        val sorted = handlers.sortedByDescending { it.priority }
        sorted.forEach {
            script.append("call handler_${it.hash}\n")
        }

        postHandle?.let { script.append("$it\n") }

        script.append("}\n\n")
        sorted.forEach {
            script.append(it.script)
        }

        // 脚本安全性检测
        try {
            this.script = script.toString().parseToScript(namespace)
        } catch (outer: Exception) {
            // 排查问题
            var index = -2
            try {
                preHandle?.parseToScript(namespace)
                index = -1
                postHandle?.parseToScript(namespace)
                index = 0
                for (i in sorted.indices) {
                    index = i
                    sorted[i].script.parseToScript(namespace)
                }

                // 未能排查问题
                outer.printStackTrace()
                console().sendLang("Dispatcher-Load-Failed-Details", id, outer.localizedMessage)
            } catch (inner: Exception) {
                // 锁定报错
                inner.printStackTrace()
                when (index) {
                    -2 -> {
                        console().sendLang("Dispatcher-Load-Failed-Details", "$id.pre-handle", inner.localizedMessage)
                    }
                    -1 -> {
                        console().sendLang("Dispatcher-Load-Failed-Details", "$id.post-handle", inner.localizedMessage)
                    }
                    else -> {
                        console().sendLang("Handler-Load-Failed-Details", sorted[index].id, inner.localizedMessage)
                    }
                }
            }
        }
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

    companion object {

        private val folder by lazy {
            File(getDataFolder(), "dispatchers")
        }

        private val mapping = ConcurrentHashMap<String, File>() // id -> File

        private val scriptCache = ConcurrentHashMap<String, Script>()

        private val cache = ConcurrentHashMap<String, EventDispatcher>()

        fun get(id: String): EventDispatcher? {
            return cache[id]
        }

        private fun onFileChanged(file: File) {
            try {
                val start = timing()

                debug("onFileChanged: ${file.name}")

                // 获取旧的调度模块
                val dispatchers = mapping.filterValues { it == file }.mapNotNull { cache.remove(it.key) }.associateBy { it.id }

                dispatchers.values.forEach {
                    // 清除缓存映射
                    mapping.remove(it.id)
                    // 注销监听器
                    EventListener.unregisterDispatcher(it)
                }

                // 记录数量
                val size = cache.size
                debug("记录数量 $size")

                // 加载文件
                loadFromFile(file, true) {

                    if (it.id in dispatchers) {
                        // 将旧对象的处理模块转移至新对象
                        it.handlers.addAll(dispatchers[it.id]!!.handlers)
                    }

                    it.postLoad()
                }

                console().sendLang("Dispatcher-Load-Automatic-Succeeded", file.name, (cache.size - size), timing(start))
            } catch (e: Exception) {
                console().sendLang("Dispatcher-Load-Automatic-Failed", file.name, e.localizedMessage)
            }
        }

        private fun loadFromFile(file: File, register: Boolean = false, func: (EventDispatcher) -> Unit = {}) {

            debug("加载文件 ${file.name}")

            // 加载文件
            file.toConfig().forEachSections { key, section ->

                debug("加载节点 $key")

                val eventName = section.getString("listen")?.let {
                    EventMapping.mapping(it) ?: error("Cannot get Dispatcher \"$key\" 's listen event mapping: \"$it\"")
                } ?: error("Dispatcher \"$key\" 's listen event is undefined!")

                val priority = section.getString("priority")?.let {
                    EventPriority.valueOf(it.uppercase())
                } ?: EventPriority.NORMAL

                val dispatcher = EventDispatcher(key, section)

                cache[key] = dispatcher
                mapping[key] = file

                func(dispatcher)

                // 注册监听器
                EventListener.registerDispatcher(dispatcher, eventName,priority, register)
            }
        }

        fun preLoad(): String {
            return try {
                // 耗时检测
                val start = timing()

                // 移除原有映射
                mapping.values.forEach {
                    it.removeWatcher()
                }
                mapping.clear()

                // 清空缓存
                cache.clear()

                folder.ifNotExists {
                    listOf(
//                        "#example.yml",
                        "def.yml"
                    ).forEach { releaseResourceFile("dispatchers/$it", true) }
                }.getFiles().forEach {

                    // 添加文件监听
                    it.addWatcher { onFileChanged(this) }

                    // 加载文件
                    loadFromFile(it)
                }

                console().asLangText("Dispatcher-Load-Succeeded", cache.size, timing(start)).also {
                    console().sendMessage(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                console().asLangText("Dispatcher-Load-Failed", e.localizedMessage).also {
                    console().sendMessage(it)
                }
            }
        }

        fun postLoad() {
            cache.values.forEach { it.postLoad() }
        }

        /**
         * @return 相关的 Dispatcher
         * */
        fun registerHandler(handler: EventHandler): List<EventDispatcher> {
            return handler.binding.mapNotNull {
                get(it)
            }.onEach {
                it.handlers += handler
            }
        }

        /**
         * @return 相关的 Dispatcher
         * */
        fun unregisterHandler(handler: EventHandler): List<EventDispatcher> {
            debug("尝试注销处理模块 ${handler.id}")
            return handler.binding.mapNotNull {
                get(it)
            }.onEach {
                it.handlers -= handler
            }
        }

        @SubscribeEvent
        fun e(e: PlayerQuitEvent) {
            cache.values.forEach { it.release(e.player) }
        }
    }


}