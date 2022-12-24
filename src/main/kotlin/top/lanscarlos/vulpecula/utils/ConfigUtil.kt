package top.lanscarlos.vulpecula.utils

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.config.VulConfig
import top.lanscarlos.vulpecula.config.VulConfigNode
import top.lanscarlos.vulpecula.config.VulConfigNodeBinding

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2022-08-19 17:05
 */

fun ConfigurationSection.getStringOrList(path: String, def: List<String> = emptyList()): List<String> {
    return when (val it = this[path]) {
        is String -> listOf(it)
        is Array<*> -> it.mapNotNull { it?.toString() }
        is Collection<*> -> it.mapNotNull { it?.toString() }
        else -> def
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

inline fun <reified T> Configuration.forEachValue(func: ((key: String, value: T) -> Unit)) {
    getKeys(false).forEach { key ->
        val it = get(key)
        if (it is T) func(key, it)
    }
}

/**
 * 加载 Config 下所有 Section 内容
 * */
inline fun Configuration.forEachSection(func: ((key: String, section: ConfigurationSection) -> Unit)) {
    getKeys(false).forEach { key ->
        getConfigurationSection(key)?.let { section ->
            func(key, section)
        }
    }
}

fun ConfigurationSection.root(): Configuration? {
    var parent = this
    while (parent.parent != null) {
        if (parent is Configuration) break
        parent = parent.parent!!
    }
    return parent as? Configuration
}

fun ConfigurationSection.wrapper(): VulConfig {
    return VulConfig(this)
}

fun <T> bindConfigNode(path: String, bind: String = "config.yml", func: ConfigurationSection.(Any?) -> T): VulConfigNode<T> {
    return VulConfigNodeBinding(path, bind, func)
}