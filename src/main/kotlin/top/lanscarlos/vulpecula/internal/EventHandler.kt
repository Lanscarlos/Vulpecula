package top.lanscarlos.vulpecula.internal

import taboolib.common.io.digest
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.internal.compiler.HandlerCompiler
import top.lanscarlos.vulpecula.utils.*
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-09-03 14:34
 */
class EventHandler(
    val id: String,
    val config: ConfigurationSection
) {

    val hash = id.digest("md5")
    val binding = config.getStringOrList("binding")
    val priority = config.getInt("priority", 8)

    val compiler = HandlerCompiler(this)

    override fun toString(): String {
        return "EventHandler(id='$id')"
    }

    companion object {

        private val folder by lazy {
            File(getDataFolder(), "handlers")
        }

        private val fileCache = mutableMapOf<File, MutableSet<String>>()
        private val cache = mutableMapOf<String, EventHandler>()

        fun get(id: String): EventHandler? = cache[id]

        fun getAll(): Collection<EventHandler> = cache.values

        private fun onFileChanged(file: File) {
            val start = timing()
            try {

                // 获取已存在的 Handler
                val existing = fileCache.remove(file)?.mapNotNull {
                    cache.remove(it)
                }?.associateBy { it.id }?.toMutableMap()

                // 加载新的 Handler
                val loaded = loadFromFile(file)

                // 受到影响的 Dispatcher
                val affected = mutableSetOf<EventDispatcher>()

                // 遍历新对象
                for (handler in loaded) {

                    if (handler.id in cache) {
                        // id 冲突
                        val conflict = fileCache.filter { handler.id in it.value }.firstNotNullOfOrNull { it.key }
                        console().sendLang("Handler-Load-Failed-Conflict", handler.id, file.canonicalPath, conflict?.canonicalPath ?: "UNKNOWN_FILE")
                        continue
                    }

                    // 尝试获取旧对象
                    val old = existing?.remove(handler.id)

                    old?.binding?.forEach {
                        if (it !in handler.binding) {
                            // 新对象解除了 Dispatcher 绑定
                            EventDispatcher.get(it)?.let { dispatcher ->
                                dispatcher.removeHandler(old.id)
                                affected += dispatcher
                            }
                        }
                    }

                    // 更新新对象所绑定的 Dispatcher
                    handler.binding.forEach {
                        EventDispatcher.get(it)?.let { dispatcher ->
                            dispatcher.addHandler(handler, true)
                            affected += dispatcher
                        }
                    }

                    // 存入缓存
                    cache[handler.id] = handler
                    // 记录文件映射信息
                    fileCache.computeIfAbsent(file) { mutableSetOf() } += handler.id
                }

                // 遍历剩余旧对象
                existing?.values?.forEach { handler ->
                    handler.binding.forEach {
                        // 解绑剩余旧对象的 Dispatcher
                        EventDispatcher.get(it)?.let { dispatcher ->
                            dispatcher.removeHandler(handler.id)
                            affected += dispatcher
                        }
                    }
                }

                // 重载所有受到影响的 Dispatcher
                affected.forEach { it.postLoad() }

                console().sendLang("Handler-Load-Automatic-Succeeded", file.name, loaded.size, timing(start))
            } catch (e: Exception) {
                console().sendLang("Handler-Load-Automatic-Failed", file.name, e.localizedMessage, timing(start))
            }
        }

        /**
         * @return 返回加载的结果
         * */
        private fun loadFromFile(file: File): Set<EventHandler> {
            val loaded = mutableSetOf<EventHandler>()
            file.toConfig().forEachSections { key, section ->
                if (section.getBoolean("enable", true)) {
                    loaded += EventHandler(key, section)
                }
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
                    ).forEach { releaseResourceFile("handlers/$it", true) }
                }.getFiles().forEach { file ->

                    loadFromFile(file).forEach inner@{ handler ->
                        if (handler.id in mapping) {
                            // id 冲突
                            val conflict = mapping[handler.id]!!
                            console().sendLang("Handler-Load-Failed-Conflict", handler.id, conflict.canonicalPath, file.canonicalPath)
                            return@inner
                        }

                        // 记录临时映射信息
                        mapping[handler.id] = file

                        // 载入缓存
                        cache[handler.id] = handler
                        // 记录文件映射信息
                        fileCache.computeIfAbsent(file) { mutableSetOf() } += handler.id
                    }

                    // 添加文件监听器
                    file.addWatcher(false) { onFileChanged(this) }
                }

                console().asLangText("Handler-Load-Succeeded", cache.size, timing(start)).also {
                    console().sendMessage(it)
                }
            } catch (e: Exception) {
                console().asLangText("Handler-Load-Failed", e.localizedMessage, timing(start)).also {
                    console().sendMessage(it)
                }
            }
        }
    }
}