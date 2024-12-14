package top.lanscarlos.vulpecula.item

import org.bukkit.entity.Item
import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.item
 *
 * @author Lanscarlos
 * @since 2024-11-28 16:59
 */
interface VolatileAPI {

    fun registerItemDespawnHandler(entity: Item)

    companion object : VolatileAPI by nmsProxy("top.lanscarlos.vulpecula.item.DefaultVolatileAPI")

}