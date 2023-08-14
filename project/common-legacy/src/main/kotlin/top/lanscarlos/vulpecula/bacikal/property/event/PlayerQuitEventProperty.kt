package top.lanscarlos.vulpecula.bacikal.property.event

import org.bukkit.event.player.PlayerQuitEvent
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
    id = "player-quit-event",
    bind = PlayerQuitEvent::class
)
class PlayerQuitEventProperty : BacikalGenericProperty<PlayerQuitEvent>("player-quit-event") {
    override fun readProperty(instance: PlayerQuitEvent, key: String): OpenResult {
        val property: Any? = when (key) {
            "message", "msg", "quit-message", "quit-msg" -> instance.quitMessage
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: PlayerQuitEvent, key: String, value: Any?): OpenResult {
        when (key) {
            "message", "msg", "quit-message", "quit-msg" -> instance.quitMessage = value?.toString()
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}