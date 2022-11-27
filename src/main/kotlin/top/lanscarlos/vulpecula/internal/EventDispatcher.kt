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
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.internal.EventListener.Companion.getListener
import top.lanscarlos.vulpecula.internal.compiler.DispatcherCompiler
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
    val config: ConfigurationSection
) {

    val eventName = config.getString("listen")?.let {
        EventMapping.mapping(it) ?: error("Cannot get Dispatcher \"$id\" 's listen event mapping: \"$it\"")
    } ?: error("Dispatcher \"$id\" 's listen event is undefined!")

    val priority = config.getString("priority")?.let {
        EventPriority.valueOf(it.uppercase())
    } ?: EventPriority.NORMAL

    val ignoreCancelled = config.getBoolean("ignore-cancelled", true)
    private val baffle = config.getConfigurationSection("baffle")?.let { initBaffle(it) }

    val handlerCache = mutableSetOf<String>()
    val handlers = mutableSetOf<String>()

    private val compiler = DispatcherCompiler(this)

    fun run(event: Event) {

        if (compiler.compiled == null) return

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

        if (baffle != null) {
            val key = player?.name ?: event.eventName
            if (!baffle.hasNext(key)) return
        }

        debug(Debug.HIGHEST, "调度器 $id 正在运行...")

        // 执行脚本
        compiler.compiled?.runActions {
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

    fun postLoad() {
        compiler.buildSource()
        compiler.compile()
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

                debug("查看旧对象 -> ${existing?.map { it.value.id }}")

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
                        dispatcher.compiler.buildSource()

                        // 比对新旧对象的脚本源码
                        if (dispatcher.compiler.source.toString() != old.compiler.source.toString()) {
                            // 脚本源码不一致，重新编译脚本
                            if (!dispatcher.compiler.compile()) {
                                // 编译失败
                                old.compiler.compiled?.let {
                                    dispatcher.compiler._compiled = it
                                }
                            }
                        }
                    } else {
                        // 不存在旧对象

                        // 获取与该 Dispatcher 绑定的 Handler
                        val handlers = EventHandler.getAll().filter { dispatcher.id in it.binding }.map { it.id }
                        dispatcher.handlerCache += handlers

                        // 构建脚本源码
                        dispatcher.compiler.buildSource()
                        // 编译脚本
                        dispatcher.compiler.compile()
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
                    debug("遍历旧对象 -> ${dispatcher.id}")
                    dispatcher.getListener()?.removeDispatcher(dispatcher.id)
                }

                console().sendLang("Dispatcher-Load-Automatic-Succeeded", file.name, loaded.size, timing(start))
            } catch (e: Exception) {
                console().sendLang("Dispatcher-Load-Automatic-Failed", file.name, e.localizedMessage, timing(start))
            }
        }

        private fun loadFromFile(file: File): Set<EventDispatcher> {
            val loaded = mutableSetOf<EventDispatcher>()
            file.toConfig().forEachSection { key, section ->
                if (section.getBoolean("disable", false)) return@forEachSection
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