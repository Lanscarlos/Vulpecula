package top.lanscarlos.vulpecula.kether.property.event

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.event
 *
 * @author Lanscarlos
 * @since 2022-10-22 21:43
 */

@VulKetherProperty(
    id = "player-command-event",
    bind = PlayerCommandPreprocessEvent::class
)
class PlayerCommandEventProperty : VulScriptProperty<PlayerCommandPreprocessEvent>("player-command-event") {

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