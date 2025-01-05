package top.lanscarlos.vulpecula.common.config

import taboolib.library.configuration.ConfigurationSection
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-01-05 14:39
 */
open class DefaultSection(val root: ConfigurationSection) : AbstractConfigSection() {

    val sections = mutableMapOf<String, ConfigNode<*>>()

    override fun contains(key: String): Boolean {
        return root.contains(key)
    }

    override fun get(key: String): Any? {
        return root[key]
    }

    override fun set(key: String, value: Any?) {
        root[key] = value
    }

    override fun remove(key: String) {
        root[key] = null
    }

    override fun <T> read(key: String, transfer: Function<Any?, T>): ConfigNode<T> {
        return DefaultNode(this, key, transfer).also { sections[key] = it }
    }

}