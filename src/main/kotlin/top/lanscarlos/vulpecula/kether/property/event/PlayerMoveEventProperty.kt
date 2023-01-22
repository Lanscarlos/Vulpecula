package top.lanscarlos.vulpecula.kether.property.event

import org.bukkit.Location
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.event
 *
 * @author Lanscarlos
 * @since 2023-01-22 22:50
 */
@VulKetherProperty(
    id = "player-move-event",
    bind = PlayerMoveEvent::class
)
class PlayerMoveEventProperty : VulScriptProperty<PlayerMoveEvent>("player-move-event") {

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