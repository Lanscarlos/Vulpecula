package top.lanscarlos.vulpecula.legacy.bacikal.property.event

import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalGenericProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.event
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:28
 */
@BacikalProperty(
    id = "player-join-event",
    bind = PlayerJoinEvent::class
)
class PlayerJoinEventProperty : BacikalGenericProperty<PlayerJoinEvent>("player-join-event") {
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