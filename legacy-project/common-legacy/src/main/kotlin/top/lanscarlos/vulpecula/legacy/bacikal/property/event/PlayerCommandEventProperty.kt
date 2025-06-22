package top.lanscarlos.vulpecula.legacy.bacikal.property.event

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalGenericProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.event
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:25
 */
@BacikalProperty(
    id = "player-command-event",
    bind = PlayerCommandPreprocessEvent::class
)
class PlayerCommandEventProperty : BacikalGenericProperty<PlayerCommandPreprocessEvent>("player-command-event") {

    override fun readProperty(instance: PlayerCommandPreprocessEvent, key: String): OpenResult {
        val property: Any = when (key) {
            "command", "cmd", "message", "msg" -> instance.message
            "player", "sender" -> instance.player
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: PlayerCommandPreprocessEvent, key: String, value: Any?): OpenResult {
        when (key) {
            "command", "cmd", "message", "msg" -> {
                instance.message = value?.toString() ?: return OpenResult.successful()
            }
            "player", "sender" -> {
                instance.player = value as? Player ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}