package top.lanscarlos.vulpecula.internal

import taboolib.common.io.digest
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.utils.*
import top.lanscarlos.vulpecula.utils.Debug.debug
import top.lanscarlos.vulpecula.utils.formatToScript
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-08-19 13:41
 */
class EventHandler(
    val id: String,
    config: ConfigurationSection
) {

    val hash = id.digest("md5")
    val binding = config.getStringOrList("binding")
    val priority = config.getInt("priority", 8)
    val namespace = config.getStringOrList("namespace")
    val variables = config.getConfigurationSection("variables")?.let { initVariables(it) }
    val handle = config["handle"]?.formatToScript()

    val script by lazy {
        buildScript()
    }

    fun buildScript(): String {
        val sb = StringBuilder("def handler_$hash = {\n")
        namespace.forEach {
            sb.append("import $it\n")
        }
        variables?.forEach { (key, value) ->
            sb.append("set $key to $value\n")
        }
        sb.append(handle)
        sb.append("\n")
        namespace.forEach {
            sb.append("release $it\n")
        }
        sb.append("}\n\n")
        return sb.toString()
    }

    private fun initVariables(config: ConfigurationSection): Map<String, String> {
        return config.getKeys(false).mapNotNull { key ->
            config.getString(key)?.let { key to it }
        }.toMap()
    }

    companion object {

        private val folder by lazy {
            File(getDataFolder(), "handlers")
        }

        private val mapping = ConcurrentHashMap<String, File>() // id -> File

        private val cache = ConcurrentHashMap<String, EventHandler>()

        fun get(id: String): EventHandler? {
            return cache[id]
        }

        private fun onFileChanged(file: File) {
            try {
                val start = timing()

                debug("onFileChanged: ${file.name}")

                // 缓存与本次加载相关联的 Handler
                val dispatchers = mutableSetOf<EventDispatcher>()

                // 获取旧的处理模块
                val handlers = mapping.filterValues { it == file }.mapNotNull { cache.remove(it.key) }

                handlers.forEach { handler ->

                    // 清除缓存映射
                    mapping.remove(handler.id)

                    // 从关联的 Dispatcher 中删除旧的 Handler
                    dispatchers.addAll(EventDispatcher.unregisterHandler(handler))
                }

                // 记录数量
                val size = cache.size
                debug("记录数量 $size")

                // 加载文件 并且 将关联的 Dispatcher 加入缓存
                dispatchers.addAll(loadFromFile(file))

                // 重构所有收到影响的 Dispatcher 脚本
                dispatchers.forEach {
                    it.postLoad()
                }

                console().sendLang("Handler-Load-Automatic-Succeeded", file.name, (cache.size - size), timing(start))
            } catch (e: Exception) {
                console().sendLang("Handler-Load-Automatic-Failed", file.name, e.localizedMessage)
            }
        }

        /**
         * @return 返回相关的 Dispatcher
         * */
        private fun loadFromFile(file: File): Set<EventDispatcher> {

            debug("加载文件 ${file.name}")

            val dispatchers = mutableSetOf<EventDispatcher>()

            // 加载文件
            file.toConfig().forEachSections { key, section ->

                debug("加载节点 $key")

                // 加载 Handler
                val handler = EventHandler(key, section)
                cache[key] = handler
                mapping[key] = file
                dispatchers.addAll(EventDispatcher.registerHandler(handler))
            }
            return dispatchers
        }

        fun load(): String {
            return try {
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
                    ).forEach { releaseResourceFile("handlers/$it", true) }
                }.getFiles().forEach {

                    // 添加文件监听
                    it.addWatcher(false) { onFileChanged(this) }

                    // 加载文件
                    loadFromFile(it)
                }

                console().asLangText("Handler-Load-Succeeded", cache.size, timing(start)).also {
                    console().sendMessage(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                console().asLangText("Handler-Load-Failed", e.localizedMessage).also {
                    console().sendMessage(it)
                }
            }
        }
    }

}