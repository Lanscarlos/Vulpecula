package top.lanscarlos.vulpecula.kether.property.event

import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.event
 *
 * @author Lanscarlos
 * @since 2023-01-19 00:30
 */
@VulKetherProperty(
    id = "player-join-event",
    bind = PlayerJoinEvent::class
)
class PlayerJoinEventProperty : VulScriptProperty<PlayerJoinEvent>("player-join-event") {
    override fun readProperty(instance: PlayerJoinEvent, key: String): OpenResult {
        val property: Any? = when (key) {
            "message", "msg", "join-message", "join-msg" -> instance.joinMessage
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: PlayerJoinEvent, key: String, value: Any?): OpenResult {
        when (key) {
            "message", "msg", "join-message", "join-msg" -> instance.joinMessage = value?.toString()
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}