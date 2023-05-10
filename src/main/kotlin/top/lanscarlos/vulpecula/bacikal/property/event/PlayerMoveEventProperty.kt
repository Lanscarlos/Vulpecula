package top.lanscarlos.vulpecula.bacikal.property.event

import org.bukkit.Location
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.bacikal.BacikalGenericProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.event
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:27
 */
@BacikalProperty(
    id = "player-move-event",
    bind = PlayerMoveEvent::class
)
class PlayerMoveEventProperty : BacikalGenericProperty<PlayerMoveEvent>("player-move-event") {

    override fun readProperty(instance: PlayerMoveEvent, key: String): OpenResult {
        val property: Any? = when (key) {
            "location-from", "loc-from", "from" -> instance.from
            "location-to", "loc-to", "to" -> instance.to
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: PlayerMoveEvent, key: String, value: Any?): OpenResult {
        when (key) {
            "location-from", "loc-from", "from" -> {
                instance.from = value as? Location ?: return OpenResult.successful()
            }
            "location-to", "loc-to", "to" -> {
                instance.setTo(value as? Location ?: return OpenResult.successful())
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}