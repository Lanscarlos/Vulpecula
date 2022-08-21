package top.lanscarlos.vulpecula.utils

import taboolib.library.configuration.ConfigurationSection

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2022-08-19 17:05
 */

fun ConfigurationSection.getStringOrList(path: String): List<String> {
    return when (val it = this[path]) {
        is String -> listOf(it)
        is List<*> -> it.mapNotNull { it?.toString() }
        else -> emptyList()
    }
}