package top.lanscarlos.vulpecula.kether.property.event

import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.event
 *
 * @author Lanscarlos
 * @since 2023-01-19 00:27
 */
@VulKetherProperty(
    id = "player-chat-event",
    bind = AsyncPlayerChatEvent::class
)
class AsyncPlayerChatEventProperty : VulScriptProperty<AsyncPlayerChatEvent>("player-chat-event") {
    override fun readProperty(instance: AsyncPlayerChatEvent, key: String): OpenResult {
        val property: Any = when (key) {
            "format" -> instance.format
            "message", "msg" -> instance.message
            "recipients" -> instance.recipients
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: AsyncPlayerChatEvent, key: String, value: Any?): OpenResult {
        when (key) {
            "format" -> {
                instance.format = value?.toString() ?: return OpenResult.successful()
            }
            "message", "msg" -> {
                instance.message = value?.toString() ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}