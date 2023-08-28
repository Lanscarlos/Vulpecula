package top.lanscarlos.vulpecula.config

import taboolib.module.configuration.ConfigLoader
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2023-08-25 00:50
 */

fun <T> bindConfigSection(path: String, bind: String = "config.yml", transfer: Function<Any?, T>): DynamicSection<T> {
    val configFile = ConfigLoader.files[bind] ?: error("Config $bind not found.")
    return DefaultDynamicConfig(configFile.file, configFile.configuration).read(path, transfer)
}