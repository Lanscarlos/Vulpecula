package top.lanscarlos.vulpecula.kether.property.event

import org.bukkit.event.player.PlayerKickEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.event
 *
 * @author Lanscarlos
 * @since 2023-01-19 00:33
 */
@VulKetherProperty(
    id = "player-kick-event",
    bind = PlayerKickEvent::class
)
class PlayerKickEventProperty : VulScriptProperty<PlayerKickEvent>("player-kick-event") {
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