package top.lanscarlos.vulpecula.common.config

import taboolib.module.configuration.ConfigLoader
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-01-05 14:56
 */
object Configs {

    fun loadFromFile(file: File): ConfigSource {
        return DefaultSource(file)
    }

    fun loadFromString(contents: String): ConfigSource {
        return DefaultSource(contents)
    }

    fun loadFromReader(reader: Reader): ConfigSource {
        return DefaultSource(reader)
    }

    fun loadFromInputStream(inputStream: InputStream): ConfigSource {
        return DefaultSource(inputStream)
    }

    fun <T> bindConfigNode(path: String, bind: String = "config.yml", transfer: Function<Any?, T>): ConfigNode<T> {
        val configuration = ConfigLoader.files[bind]?.configuration ?: error("Config $bind not found.")
        return DefaultSource(configuration).read(path, transfer)
    }

}