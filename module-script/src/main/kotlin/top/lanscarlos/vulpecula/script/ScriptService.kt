package top.lanscarlos.vulpecula.script

import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.Configs
import top.lanscarlos.vulpecula.common.config.ConfigServiceCallback
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.script
 *
 * @author Lanscarlos
 * @since 2025/4/25 10:00
 */
object ScriptService {

    val directory: File = File(getDataFolder(), "script")

    private val scripts: HashMap<String, Script> = hashMapOf()

    init {
        // 注册工作空间
        Configs.register(id = "script", directory = directory, priority = 8, callback = Callback)
    }

    fun get(id: String): Script? = scripts[id]

    fun keys(): Set<String> = scripts.keys

    fun values(): Collection<Script> = scripts.values

    fun entries(): Set<Map.Entry<String, Script>> = scripts.entries

    internal object Callback : ConfigServiceCallback {

        override fun onFileDeleted(id: String, file: File) {
            scripts.remove(id)
        }

        override fun onFileCreated(id: String, file: File) {
            val script = when (file.extension) {
                "ks" -> NativeScript(id, file)
                "yml", "yaml" -> CompiledScript(id, Configuration.loadFromFile(file))
                else -> error("Unsupported file extension ${file.extension}")
            }
            scripts[id] = script
        }

        override fun onFileModified(id: String, file: File) {
            when (val script = scripts[id]!!) {
                is NativeScript -> {
                    // 直接重新创建
                    onFileCreated(id, file)
                }
                is CompiledScript -> {
                    // 刷新配置
                    script.config.loadFromFile(file)
                }
                else -> error("Unknown script type ${script::class.java}")
            }
        }
    }

}