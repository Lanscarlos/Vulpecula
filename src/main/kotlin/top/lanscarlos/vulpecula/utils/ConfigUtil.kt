package top.lanscarlos.vulpecula.utils

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration

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

/**
 * 加载 Config 下所有 String 内容
 * */
inline fun ConfigurationSection.forEachLine(func: ((key: String, value: String) -> Unit)) {
    getKeys(false).forEach { key ->
        getString(key)?.let { func(key, it) }
    }
}

inline fun <reified T> Configuration.forEachObject(func: ((key: String, value: T) -> Unit)) {
    getKeys(false).forEach { key ->
        val it = get(key)
        if (it is T) func(key, it)
    }
}

/**
 * 加载 Config 下所有 Section 内容
 * */
inline fun Configuration.forEachSections(func: ((key: String, section: ConfigurationSection) -> Unit)) {
    getKeys(false).forEach { key ->
        getConfigurationSection(key)?.let { section ->
            func(key, section)
        }
    }
}