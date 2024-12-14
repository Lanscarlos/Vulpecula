package top.lanscarlos.vulpecula

import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.player.PlayerDropItemEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2023-08-14 17:07
 */
object Vulpecula {

    @Config("config.conf")
    var config: Configuration? = null
        private set

//    @SubscribeEvent
//    fun e(e: EntitySpawnEvent) {
//        info("EntitySpawnEvent >> ${e.entity.type.name}")
//        NmsHandler.handleEntityRemove(e.entity)
//    }

//    @SubscribeEvent
    fun e(e: PlayerDropItemEvent) {
        info("PlayerDropItemEvent >> ${e.itemDrop.type.name}")
        NmsHandler.handleEntityRemove(e.itemDrop)
    }

//    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        info("EntityDamageEvent >> remove ${e.entity.type.name}")
        e.entity.remove()
//        val type = e.entity.type.name
//        val cause = e.cause.name
//        val reference = e.entity.javaClass.name
//        info("EntityDamageEvent >> type=$type; cause=$cause; reference=$reference")
//        if (type == "DROPPED_ITEM") {
//            e.isCancelled = true
//        }
    }

//    @SubscribeEvent
    fun e(e: EntityDeathEvent) {
        val type = e.entity.type.name
        val cause = e.entity.lastDamageCause?.cause?.name
        val reference = e.entity.javaClass.name
        info("EntityDeathEvent >> type=$type; cause=$cause; reference=$reference")
    }

//    @SubscribeEvent
    fun e(e: ItemDespawnEvent) {
        val type = e.entity.type.name
        val reference = e.entity.javaClass.name
        info("ItemDespawnEvent >> type=$type; reference=$reference")
//        if (type == "DROPPED_ITEM") {
//            e.isCancelled = true
//        }
    }

}