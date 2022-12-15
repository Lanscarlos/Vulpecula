package top.lanscarlos.vulpecula.config

import taboolib.library.configuration.ConfigurationSection
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2022-12-15 19:16
 */
class VulConfig(
    config: ConfigurationSection
) {

    var source: ConfigurationSection = config
        private set

    val nodes = CopyOnWriteArraySet<VulConfigNode<*>>()

    fun <T: Any> read(path: String, transfer: ConfigurationSection.(Any?) -> T): VulConfigNode<T> {
        return VulConfigNodeTransfer(path, this, transfer).also {
            nodes += it
        }
    }

    fun updateSource(config: ConfigurationSection): List<String> {
        this.source = config
        return nodes.mapNotNull {
            if (it !is VulConfigNodeTransfer<*>) return@mapNotNull null
            if (it.update(config)) it.path else null
        }
    }
}