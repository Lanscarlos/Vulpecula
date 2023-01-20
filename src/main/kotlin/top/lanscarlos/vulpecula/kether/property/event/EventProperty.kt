package top.lanscarlos.vulpecula.kether.property.event

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.block.BlockEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.event.player.PlayerEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty
import top.lanscarlos.vulpecula.utils.coerceBoolean

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.event
 *
 * @author Lanscarlos
 * @since 2022-10-21 10:59
 */

@VulKetherProperty(
    id = "event",
    bind = Event::class
)
class EventProperty : VulScriptProperty<Event>("event") {

    override fun readProperty(instance: Event, key: String): OpenResult {
        val property: Any? = when (key) {
            "block" -> (instance as? BlockEvent)?.block
            "entity" -> (instance as? EntityEvent)?.entity
            "inventory", "inv" -> (instance as? InventoryEvent)?.inventory
            "player" -> (instance as? PlayerEvent)?.player
            "eventName", "event-name", "name" -> instance.eventName
            "isAsynchronous", "asynchronous", "async" -> instance.isAsynchronous
            "isCancelled", "cancelled", "cancel" -> (instance as? Cancellable)?.isCancelled ?: false
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: Event, key: String, value: Any?): OpenResult {
        when (key) {
            "isCancelled", "cancelled", "cancel" -> {
                (instance as? Cancellable)?.let {
                    it.isCancelled = value?.coerceBoolean() ?: return OpenResult.successful()
                }
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}