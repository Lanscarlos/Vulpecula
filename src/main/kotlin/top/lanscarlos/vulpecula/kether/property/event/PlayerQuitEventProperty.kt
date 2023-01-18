package top.lanscarlos.vulpecula.kether.property.event

import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.event
 *
 * @author Lanscarlos
 * @since 2023-01-19 00:32
 */
@VulKetherProperty(
    id = "player-quit-event",
    bind = PlayerQuitEvent::class
)
class PlayerQuitEventProperty : VulScriptProperty<PlayerQuitEvent>("player-quit-event") {
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