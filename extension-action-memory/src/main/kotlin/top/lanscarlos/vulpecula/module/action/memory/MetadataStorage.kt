package top.lanscarlos.vulpecula.module.action.memory

import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable
import taboolib.platform.BukkitPlugin
import top.lanscarlos.vulpecula.common.core.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.memory
 *
 * @author Lanscarlos
 * @since 2025/7/1 14:10
 */
object MetadataStorage : MemoryStorage {

    override fun get(key: String, owner: Any): Any? {
        val metadatable = getMetadatable(owner)
        return metadatable.getMetadata(key).firstOrNull { it.owningPlugin == BukkitPlugin.getInstance() }
    }

    override fun set(key: String, value: Any, owner: Any): Boolean {
        val metadatable = getMetadatable(owner)
        val exist = get(key, owner) != null
        metadatable.setMetadata(key, FixedMetadataValue(BukkitPlugin.getInstance(), value))
        return exist
    }

    override fun remove(key: String, owner: Any): Any? {
        val metadatable = getMetadatable(owner)
        val exist = get(key, owner)
        metadatable.removeMetadata(key, BukkitPlugin.getInstance())
        return exist
    }

    private fun getMetadatable(owner: Any): Metadatable {
        return owner as? Metadatable ?: error(asLang("module-action-memory-exception-unsupported-owner-type", "Metadata", owner::class.java.name))
    }

}