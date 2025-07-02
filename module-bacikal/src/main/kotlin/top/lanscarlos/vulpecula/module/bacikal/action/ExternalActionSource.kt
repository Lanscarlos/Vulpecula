package top.lanscarlos.vulpecula.module.bacikal.action

import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import top.lanscarlos.vulpecula.common.core.utils.asLang
import java.io.File
import java.io.FileOutputStream

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.action
 *
 * @author Lanscarlos
 * @since 2025/7/2 10:12
 */
class ExternalActionSource(resources: Map<String, ByteArray>) : ActionSource {

    override val name: String

    override val version: String

    override val authors: List<String>

    val config: Configuration

    private val metadata: HashMap<String, Array<String>> = hashMapOf()

    init {
        val registry = initActionRegistry(resources)
        name = registry.getString("name") ?: "UNKNOWN_NAME"
        version = registry.getString("version") ?: "UNKNOWN_VERSION"
        authors = registry.getStringList("authors")
        config = initActionConfig(resources)

        // 解析 metadata
        for ((name, byteArray) in resources) {
            if (!name.startsWith("metadata/")) {
                continue
            }
            if (!name.endsWith(".metadata")) {
                continue
            }
            val key = name.substringAfterLast("/").substringBefore('.')
            val array = BuiltInActionSource.decodeMetadata(byteArray)
            metadata[key] = array
        }
    }

    override fun getActionMetadata(name: String): Array<String> {
        return metadata[name] ?: error("Metadata $name not found.")
    }

    private fun initActionConfig(resources: Map<String, ByteArray>): Configuration {
        val file = File(getDataFolder(), "config/${name}.yml")
        if (file.exists()) {
            return Configuration.loadFromFile(file)
        }
        file.parentFile.mkdirs()
        val inputStream = resources.entries.find { it.key == "config.yml" }?.value?.inputStream()
            ?: return Configuration.empty()
        // 输出文件
        inputStream.use {
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return Configuration.loadFromInputStream(inputStream)
    }

    private fun initActionRegistry(resources: Map<String, ByteArray>): Configuration {
        val inputStream = resources.entries.find { it.key == "plugin.yml" }?.value?.inputStream()
            ?: error(asLang("Registry not found."))
        return Configuration.loadFromInputStream(inputStream, Type.YAML)
    }

}