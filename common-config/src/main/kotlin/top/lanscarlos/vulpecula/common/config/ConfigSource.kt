package top.lanscarlos.vulpecula.common.config

import java.io.File
import java.io.InputStream
import java.io.Reader
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2024-12-16 01:35
 */
interface ConfigSource : ConfigSection {

    fun reload()

    fun reloadFromFile(file: File)

    fun reloadFromString(contents: String)

    fun reloadFromReader(reader: Reader)

    fun reloadFromInputStream(inputStream: InputStream)

}