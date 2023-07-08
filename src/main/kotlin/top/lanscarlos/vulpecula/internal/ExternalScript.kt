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
import top.lanscarlos.vulpecula.bacikal.script.BacikalScriptBuilder
import top.lanscarlos.vulpecula.bacikal.script.ScriptTransfer
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
class ExternalScript(
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

    val functions by wrapper.read("functions") { value ->
        val config = value as? ConfigurationSection ?: return@read emptyList()
        config.getKeys(false).map { name ->
            val section = config.getConfigurationSection(name)!!

            BacikalScriptBuilder().apply {
                // 修正函数名
                this.name = name

                /* 构建参数转换 */
                section.getStringList("args").let { args ->
                    if (args.isNotEmpty()) {
                        for ((i, it) in args.withIndex()) {
                            appendPreprocessor("set $it to &arg$i\n")
                        }
                        appendPreprocessor("\n")
                    }
                }

                appendContent(section.getString("content"))
                appendCondition(section.getString("condition"))
                appendDeny(section.getString("deny"))
                appendExceptions(section.getConfigurationSection("exception"))
                appendVariables(section.getConfigurationSection("variables"))
            }
        }
    }

    lateinit var source: String

    init {
        // 编译脚本
        if (autoCompile) compileScript()
    }

    fun compileScript() {
        try {
            // 尝试构建脚本
            this.source = buildBacikalScript(namespace, false) {
                appendCondition(this@ExternalScript.condition)
                appendDeny(this@ExternalScript.deny)
                appendContent(this@ExternalScript.main)
                appendExceptions(this@ExternalScript.exception)
                appendVariables(this@ExternalScript.variables)

                for (it in this@ExternalScript.functions) {
                    appendFunction(it)
                }

                // 添加局部碎片替换
                addTransfer(object : ScriptTransfer {
                    override fun transfer(source: StringBuilder) {
                        if (fragments.isNotEmpty()) {
                            val keys = fragments.keys.joinToString("|")
                            val pattern = "\\\$($keys)(?=\\b)|\\\$\\{($keys)}".toPattern()
                            val matcher = pattern.matcher(source.extract())
                            val buffer = StringBuffer()

                            while (matcher.find()) {
                                val found = matcher.group().substring(1).let {
                                    if (it.startsWith('{') || it.endsWith('}')) {
                                        it.substring(1, it.lastIndex)
                                    } else it
                                }
                                matcher.appendReplacement(buffer, fragments[found] ?: "")
                            }
                            // 兼容 Github 构建系统
                            source.append(matcher.appendTail(buffer))
                        }
                    }
                })
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

        val cache = mutableMapOf<String, ExternalScript>()

        fun get(id: String): ExternalScript? = cache[id]

        fun getAll(): Collection<ExternalScript> = cache.values

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
                    cache[name] = ExternalScript(name, file.toConfig().toDynamic())
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