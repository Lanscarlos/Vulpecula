package top.lanscarlos.vulpecula.bacikal.property.event

import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.bacikal.BacikalGenericProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.event
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:21
 */
@BacikalProperty(
    id = "player-chat-event",
    bind = AsyncPlayerChatEvent::class
)
class AsyncPlayerChatEventProperty : BacikalGenericProperty<AsyncPlayerChatEvent>("player-chat-event") {
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