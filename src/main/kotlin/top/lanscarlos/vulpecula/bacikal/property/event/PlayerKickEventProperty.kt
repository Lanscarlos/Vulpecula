package top.lanscarlos.vulpecula.bacikal.property.event

import org.bukkit.event.player.PlayerKickEvent
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
    id = "player-kick-event",
    bind = PlayerKickEvent::class
)
class PlayerKickEventProperty : BacikalGenericProperty<PlayerKickEvent>("player-kick-event") {
    override fun readProperty(instance: PlayerKickEvent, key: String): OpenResult {
        val property: Any = when (key) {
            "message", "msg", "leave-message", "leave-msg" -> instance.leaveMessage
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: PlayerKickEvent, key: String, value: Any?): OpenResult {
        when (key) {
            "message", "msg", "leave-message", "leave-msg" -> instance.leaveMessage = value?.toString() ?: return OpenResult.successful()
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}