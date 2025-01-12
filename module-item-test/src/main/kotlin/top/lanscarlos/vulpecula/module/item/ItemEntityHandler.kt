package top.lanscarlos.vulpecula.module.item

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import org.bukkit.entity.Item
import org.bukkit.event.entity.EntitySpawnEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.item
 *
 * @author Lanscarlos
 * @since 2024-11-28 16:58
 */
object ItemEntityHandler {

//    @SubscribeEvent
//    fun e(e: EntitySpawnEvent) {
//        info("EntitySpawnEvent >> ${e.entity.type.name}")
//        val item = e.entity as? Item ?: return
//        VolatileAPI.registerItemDespawnHandler(item)
//    }

    @SubscribeEvent
    fun e(e: PlayerArmorChangeEvent) {
        info("PlayerArmorChangeEvent >> ${e.player.name}; slot=${e.slotType.name}; old=${e.oldItem}; new=${e.newItem}")
    }

    @SubscribeEvent
    fun e(e: EntityRemoveFromWorldEvent) {
        info("EntityRemoveFromWorldEvent >> ${e.entity.type.name}; slot=${e.world.name}")
    }

}