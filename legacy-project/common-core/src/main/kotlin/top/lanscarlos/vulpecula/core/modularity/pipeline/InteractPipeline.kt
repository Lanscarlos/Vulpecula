package top.lanscarlos.vulpecula.core.modularity.pipeline

import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import top.lanscarlos.vulpecula.config.DynamicConfig

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.modularity.pipeline
 *
 * @author Lanscarlos
 * @since 2023-12-28 01:09
 */
class InteractPipeline(config: DynamicConfig) : AbstractPipeline<PlayerInteractEvent>(config) {

    val filterLeftHand by config.readBoolean("filter-left-hand", false)

    val filterRightHand by config.readBoolean("filter-right-hand", true)

    val filterClickAir by config.readBoolean("filter-click-air", true)

    val filterClickBlock by config.readBoolean("filter-click-block", false)

    override fun filter(event: PlayerInteractEvent): Boolean {
        return when {
            filterLeftHand && event.hand == EquipmentSlot.HAND -> false
            filterRightHand && event.hand == EquipmentSlot.OFF_HAND -> false
            filterClickAir && (event.action == Action.LEFT_CLICK_AIR || event.action == Action.RIGHT_CLICK_AIR) -> false
            filterClickBlock && (event.action == Action.LEFT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_BLOCK) -> false
            else -> true
        }
    }

    override fun preprocess(event: PlayerInteractEvent) {
    }

    override fun variables(event: PlayerInteractEvent): Map<String, Any?> {
        TODO("Not yet implemented")
    }
}