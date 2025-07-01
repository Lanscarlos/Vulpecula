package top.lanscarlos.vulpecula.module.bacikal.action

import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.io.FileOutputStream

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.action
 *
 * @author Lanscarlos
 * @since 2025/7/1
 */
abstract class ExternalAction : ActionSource {

    final override val name: String

    final override val version: String

    final override val authors: List<String>

    val config: Configuration

    init {
        val registry = getRegistry()
        name = registry.getString("name") ?: "UNKNOWN_NAME"
        version = registry.getString("version") ?: "UNKNOWN_VERSION"
        authors = registry.getStringList("authors")
        config = getConfig()
    }

    open fun onInit() = Unit

    open fun onEnable() = Unit

    open fun onActive() = Unit

    open fun onDisable() = Unit

    private fun getConfig(): Configuration {
        val file = File(getDataFolder(), "config/${name}.yml")
        if (file.exists()) {
            return Configuration.loadFromFile(file)
        }
        val inputStream = this.javaClass.classLoader.getResourceAsStream("config.yml")
            ?: return Configuration.empty()

        // 输出文件
        inputStream.use {
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return Configuration.loadFromInputStream(inputStream)
    }

    private fun getRegistry(): Configuration {
        val inputStream = this.javaClass.classLoader.getResourceAsStream("plugin.yml")!!
        return Configuration.loadFromInputStream(inputStream, Type.YAML)
    }

}