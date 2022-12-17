package top.lanscarlos.vulpecula.kether.property.event

import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.event
 *
 * @author Lanscarlos
 * @since 2022-11-12 12:53
 */

@VulKetherProperty(
    id = "player-item-held-event",
    bind = PlayerItemHeldEvent::class
)
class PlayerItemHeldEventProperty : VulScriptProperty<PlayerItemHeldEvent>("player-item-held-event") {

    override fun readProperty(instance: PlayerItemHeldEvent, key: String): OpenResult {
        val property: Any? = when (key) {
            "newSlot", "new-slot" -> instance.newSlot
            "previousSlot", "previous-slot", "old-slot" -> instance.previousSlot
            "new-item" -> instance.player.inventory.getItem(instance.newSlot)
            "previous-item","old-item" -> instance.player.inventory.getItem(instance.previousSlot)
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: PlayerItemHeldEvent, key: String, value: Any?): OpenResult {
        when (key) {
            "new-item" -> {
                instance.player.inventory.setItem(instance.newSlot, value as? ItemStack)
            }
            "previous-item","old-item" -> {
                instance.player.inventory.setItem(instance.previousSlot, value as? ItemStack)
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}