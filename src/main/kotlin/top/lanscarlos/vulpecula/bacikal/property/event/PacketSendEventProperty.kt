package top.lanscarlos.vulpecula.bacikal.property.event

import org.bukkit.entity.Player
import taboolib.common.OpenResult
import taboolib.module.nms.PacketSendEvent
import top.lanscarlos.vulpecula.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.bacikal.BacikalScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.event
 *
 * @author Lanscarlos
 * @since 2023-04-12 09:18
 */
@BacikalProperty(
    id = "packet-send-event",
    bind = PacketSendEvent::class
)
class PacketSendEventProperty : BacikalScriptProperty<PacketSendEvent>("packet-send-event") {

    override fun readProperty(instance: PacketSendEvent, key: String): OpenResult {
        val property: Any = when (key) {
            "player", "receiver" -> instance.player
            "packet" -> instance.packet
            "name" -> instance.packet.name
            "fully-name" -> instance.packet.fullyName
            "source" -> instance.packet.source
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: PacketSendEvent, key: String, value: Any?): OpenResult {
        if (key == "packet") {
            instance.packet.overwrite(value ?: return OpenResult.successful())
        } else {
            try {
                instance.packet.write(key, value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return OpenResult.successful()
    }
}