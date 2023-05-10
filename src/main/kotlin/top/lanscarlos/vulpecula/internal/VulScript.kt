package top.lanscarlos.vulpecula.internal

import taboolib.common.io.newFile
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common5.cbool
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.bacikal.buildBacikalScript
import top.lanscarlos.vulpecula.config.DynamicConfig
import top.lanscarlos.vulpecula.config.DynamicConfig.Companion.bindConfigNode
import top.lanscarlos.vulpecula.config.DynamicConfig.Companion.toDynamic
import top.lanscarlos.vulpecula.utils.*
import top.lanscarlos.vulpecula.utils.Debug.debug
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-12-23 12:11
 */
class VulScript(
    val id: String,
    val wrapper: DynamicConfig
) {

    val targetPath by wrapper.read("build-setting.target-path") { value ->
        val def = id.substringBeforeLast('.').replace('.', File.separatorChar) + ".ks"
        val path = value?.toString()?.let {
            if (!it.substringAfterLast('/').contains('.')) "$it.ks" else it
        }

        if (path?.startsWith("/") == true) {
            File(".$path")
        } else {
            File(ScriptWorkspace.folder, path ?: def)
        }
    }

    val targetOverride by wrapper.readBoolean("build-setting.target-override", true)

    val escapeUnicode by wrapper.readBoolean("build-setting.escape-unicode", false)

    val autoCompile by wrapper.readBoolean("build-setting.auto-compile", true)

    val namespace by wrapper.readStringList("namespace")

    val main by wrapper.read("main")
    val variables by wrapper.read("variables")
    val condition by wrapper.read("condition")
    val deny by wrapper.read("deny")
    val exception by wrapper.read("exception")

    val fragments by wrapper.read("fragments") { value ->
        val config = value as? ConfigurationSection ?: return@read mapOf()
        config.getKeys(false).associateWith {
            config.getString(it)!!
        }
    }

    val functions = mutableMapOf<String, VulScriptFunction>()

    lateinit var source: String

    init {
        /*
        * 加载自定义函数
        *
        * 如果 functions 发生变化，则下面的函数会被调用
        * 此时可以类似于 Handler 通知 Function 检查更新
        * */
        wrapper.source.getConfigurationSection("functions")?.let {
            initFunctions(it)
        }
        wrapper.read("functions") { value ->
            initFunctions(value as? ConfigurationSection ?: return@read)
        }

        // 编译脚本
        if (autoCompile) compileScript()
    }

    fun compileScript() {
        try {
            // 尝试构建脚本
            this.source = buildBacikalScript(namespace) {

            }.source

            // 导出脚本
            if (!targetPath.exists()) {
                // 脚本文件不存在
                newFile(targetPath, create = true)
            } else if (!targetOverride) {
                // 不覆盖脚本文件
                return
            }
            targetPath.outputStream().use {
                it.write(source.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initFunctions(config: ConfigurationSection) {
        val keys = config.getKeys(false).toMutableSet()

        if (functions.isNotEmpty()) {
            // 遍历已存在的函数
            val iterator = functions.iterator()
            while (iterator.hasNext()) {
                val function = iterator.next().value
                if (function.id in keys) {
                    // 函数仍然存在，尝试更新属性
                    debug(Debug.HIGH, "Function contrasting \"${function.id}\" at Script \"$id\"")
                    function.contrast(config.getConfigurationSection(function.id)!!)

                    // 移除该 id
                    keys -= function.id
                } else {
                    // 函数已被用户移除
                    iterator.remove()
                    debug(Debug.HIGH, "Function delete \"${function.id}\" at Script \"$id\"")
                }
            }
        }

        // 遍历新的函数
        for (key in keys) {
            val section = config.getConfigurationSection(key) ?: continue
            functions[key] = VulScriptFunction(key, section.toDynamic())
            debug(Debug.HIGH, "Function delete \"$key\" at Script \"$id\"")
        }
    }

    /**
     * 对照并尝试更新
     * */
    fun contrast(section: Configuration) {
        if (wrapper.updateSource(section).isNotEmpty()) {
            if (autoCompile) compileScript()
        }
    }

    companion object {

        val automaticReload by bindConfigNode("automatic-reload.script-source") {
            it?.cbool ?: false
        }

        val folder = File(getDataFolder(), "scripts")

        val cache = mutableMapOf<String, VulScript>()

        fun get(id: String): VulScript? = cache[id]

        fun getAll(): Collection<VulScript> = cache.values

        fun onFileChanged(file: File) {
            if (!automaticReload) {
                file.removeWatcher()
                return
            }

            val start = timing()
            try {
                val name = folder.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '.')
                val script = cache[name] ?: error("Script source \"$name\" is not exist.")
                debug(Debug.HIGH, "Script contrasting \"$name\"")
                script.contrast(file.toConfig())

                console().sendLang("Script-Compile-Load-Automatic-Succeeded", file.name, timing(start))
            } catch (e: Exception) {
                console().sendLang("Script-Compile-Load-Automatic-Failed", file.name, e.localizedMessage, timing(start))
            }
        }

        fun load(): String {
            val start = timing()
            return try {
                cache.clear()

                val folder = folder.toPath()
                val ignored = ScriptWorkspace.folder.toPath()

                if (java.nio.file.Files.notExists(folder)) {
                    // 路径不存在
                    releaseResourceFile("scripts/#def.yml", true)
                }

                // 遍历文件
                val iterator = java.nio.file.Files.walk(folder).iterator()
                while (iterator.hasNext()) {
                    val path = iterator.next()

                    // 排除
                    if (java.nio.file.Files.isDirectory(path)) continue
                    if (path.startsWith(ignored)) continue
                    if (path.toString().let { !it.endsWith(".yml") && !it.endsWith("yaml") }) continue
                    if (path.fileName.toString().startsWith("#")) continue

                    val name = folder.relativize(path).toString().replace(File.separatorChar, '.')
                    val file = path.toFile().apply {
                        if (automaticReload) addWatcher(false) { onFileChanged(this) }
                    }
                    cache[name] = VulScript(name, file.toConfig().toDynamic())
                }

                console().asLangText("Script-Compile-Load-Succeeded", cache.size, timing(start)).also {
                    console().sendMessage(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                console().asLangText("Script-Compile-Load-Failed", e.localizedMessage, timing(start)).also {
                    console().sendMessage(it)
                }
            }
        }
    }
}