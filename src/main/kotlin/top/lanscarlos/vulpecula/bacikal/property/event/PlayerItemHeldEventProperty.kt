package top.lanscarlos.vulpecula.bacikal.property.event

import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.bacikal.BacikalGenericProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.event
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:29
 */
@BacikalProperty(
    id = "player-item-held-event",
    bind = PlayerItemHeldEvent::class
)
class PlayerItemHeldEventProperty : BacikalGenericProperty<PlayerItemHeldEvent>("player-item-held-event") {

    override fun readProperty(instance: PlayerItemHeldEvent, key: String): OpenResult {
        val property: Any? = when (key) {
            "new-slot", "slot" -> instance.newSlot
            "previous-slot", "old-slot" -> instance.previousSlot
            "new-item", "item" -> instance.player.inventory.getItem(instance.newSlot)
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