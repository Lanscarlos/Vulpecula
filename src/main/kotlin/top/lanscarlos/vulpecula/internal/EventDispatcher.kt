package top.lanscarlos.vulpecula.internal

import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.internal.EventListener.Companion.getListener
import top.lanscarlos.vulpecula.utils.*
import java.io.File

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

    val scriptSource = ""

    val handlerCache = mutableSetOf<String>()
    val handlers = mutableSetOf<String>()

    /**
     * 通过脚本源码构建脚本对象
     * */
    fun buildScript() {}

    fun addHandler(id: String) {
        handlerCache += id
    }

    fun removeHandler(id: String) {
        handlerCache -= id
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
                        dispatcher.handlers += old.handlers

                        // 比对新旧对象的监听器/事件要素
                        if (listener.id != old.getListener()?.id) {
                            // 新对象发生改变，更改监听器
                            listener.addDispatcher(dispatcher)
                            old.getListener()?.removeDispatcher(old.id)
                        }

                        if (dispatcher.scriptSource != old.scriptSource) {
                            // 脚本源码不一致，重构脚本
                            dispatcher.buildScript()
                        }
                    } else {
                        // 不存在旧对象

                        // 获取与该 Dispatcher 绑定的 Handler
                        val handlers = EventHandler.getAll().filter { dispatcher.id in it.binding }.map { it.id }
                        dispatcher.handlers += handlers
                    }

                    // 尝试注册监听器
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
    }
}