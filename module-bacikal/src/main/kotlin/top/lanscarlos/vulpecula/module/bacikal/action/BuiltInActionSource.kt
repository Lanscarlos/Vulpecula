package top.lanscarlos.vulpecula.module.bacikal.action

import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.pluginVersion
import java.io.InputStream
import java.util.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.action
 *
 * @author Lanscarlos
 * @since 2025/6/18 17:23
 */
object BuiltInActionSource : ActionSource {

    override val name: String by lazy { pluginId }

    override val version: String by lazy { pluginVersion }

    override val authors: List<String> = listOf("Lanscarlos")

    override fun getActionMetadata(name: String): Array<String> {
        val stream = this.javaClass.classLoader.getResourceAsStream("metadata/${name}.metadata")
            ?: error("Metadata $name not found.")
        return decodeMetadata(stream)
    }

    private fun decodeMetadata(stream: InputStream): Array<String> {
        return decodeMetadata(byteArray = stream.readAllBytes())
    }

    internal fun decodeMetadata(byteArray: ByteArray): Array<String> {
        return String(byteArray)
            .split("\\R".toRegex())
            .map { Base64.getDecoder().decode(it).toString(Charsets.ISO_8859_1) }
            .toTypedArray()
    }

}