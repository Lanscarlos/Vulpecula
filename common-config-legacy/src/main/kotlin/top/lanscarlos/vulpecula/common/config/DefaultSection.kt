package top.lanscarlos.vulpecula.common.config

import taboolib.library.configuration.ConfigurationSection

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-01-05 14:39
 */
open class DefaultSection(val parent: ConfigSection?, val root: ConfigurationSection) : AbstractConfigSection() {

    override val path: String = root.name

    val nodes = mutableMapOf<String, ConfigNode>()

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

    override fun getSection(key: String): ConfigSection {
        return DefaultSection(this, root.getConfigurationSection(key) ?: root.createSection(key))
    }

    override fun read(vararg key: String): ConfigNode {
        return DefaultNode(this, key)
    }

}