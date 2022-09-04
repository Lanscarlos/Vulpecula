package top.lanscarlos.vulpecula.internal

import taboolib.common.io.digest
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
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
    config: ConfigurationSection
) {

    val hash = id.digest("md5")
    val binding = config.getStringOrList("binding")
    val priority = config.getInt("priority", 8)
    val namespace = config.getStringOrList("namespace")
    private val exception = config["exception"]?.let { initException(it) }
    private val variables = config.getConfigurationSection("variables")?.let { initVariables(it) }
    private val handle = config["handle"]?.formatToScript()

    val script by lazy {
        buildScript()
    }

    private fun buildScript(): String {
        val sb = StringBuilder("def handler_$hash = {\n")
        namespace.forEach {
            sb.append("import $it\n")
        }
        variables?.forEach { (key, value) ->
            sb.append("set $key to $value\n")
        }
        if (exception != null) {
            sb.append("try {\n")
        }
        sb.append(handle)
        sb.append("\n")
        if (exception != null) {
            sb.append("} $exception\n\n")
        }
        namespace.forEach {
            sb.append("release $it\n")
        }
        sb.append("}\n\n")
        return sb.toString()
    }

    fun compileChecking(): Boolean {
        var pointer = "Uninitialized"
        return try {
            variables?.forEach { (key, source) ->
                pointer = "variables.$key"
                source.parseToScript(namespace)
            }

            pointer = "handle"
            handle?.parseToScript(namespace)
            true
        } catch (e: Exception) {
            console().sendLang("Handler-Load-Failed-Details", id, pointer, e.localizedMessage)
            false
        }
    }

    private fun initException(content: Any): String {
        val sb = StringBuilder("catch ")
        when (content) {
            is String -> {
                sb.append("{\n$content\n}")
            }
            is Map<*, *> -> {
                when (val type = content["catch"]) {
                    is String -> sb.append("with \"$type\" ")
                    is List<*> -> sb.append("with \"${type.distinct().joinToString(separator = "|")}\" ")
                }
                sb.append("{\n${content["handle"]}\n}")
            }
            is ConfigurationSection -> {
                when (val type = content["catch"]) {
                    is String -> sb.append("with \"$type\" ")
                    is List<*> -> sb.append("with \"${type.distinct().joinToString(separator = "|")}\" ")
                }
                sb.append("{\n${content["handle"]}\n}")
            }
            is List<*> -> {
                val types = mutableSetOf<String>()
                val temp = StringBuilder()

                content.forEach { element ->
                    val meta = element as? Map<*, *> ?: return@forEach

                    if (temp.isEmpty()) {
                        temp.append("if ")
                    } else {
                        temp.append("else if ")
                    }

                    when (val type = meta["catch"]) {
                        is String -> {
                            types += type
                            temp.append("check &exception == \"$type\"")
                        }
                        is List<*> -> {
                            types += type.mapNotNull { it?.toString() }
                            temp.append("any [")
                            type.forEach {
                                temp.append(" check &exception == \"$it\"")
                            }
                            temp.append(" ]")
                        }
                        else -> temp.append("true")
                    }

                    temp.append(" then {\n")
                    temp.append(meta["handle"])
                    temp.append("\n}\n")
                }

                sb.append("with \"${types.joinToString(separator = "|")}\"")
                sb.append("{\n$temp}")
            }
        }
        return sb.toString()
    }

    private fun initVariables(config: ConfigurationSection): Map<String, String> {
        return config.getKeys(false).mapNotNull { key ->
            config.getString(key)?.let { key to it }
        }.toMap()
    }

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
                loaded += EventHandler(key, section)
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