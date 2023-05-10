package top.lanscarlos.vulpecula.bacikal.property.event

import taboolib.common.OpenResult
import taboolib.module.nms.PacketReceiveEvent
import top.lanscarlos.vulpecula.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.bacikal.BacikalGenericProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.event
 *
 * @author Lanscarlos
 * @since 2023-04-12 09:18
 */
@BacikalProperty(
    id = "packet-receive-event",
    bind = PacketReceiveEvent::class
)
class PacketReceiveEventProperty : BacikalGenericProperty<PacketReceiveEvent>("packet-receive-event") {

    override fun readProperty(instance: PacketReceiveEvent, key: String): OpenResult {
        val property: Any = when (key) {
            "player", "sender" -> instance.player
            "packet" -> instance.packet
            "name" -> instance.packet.name
            "fully-name" -> instance.packet.fullyName
            "source" -> instance.packet.source
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: PacketReceiveEvent, key: String, value: Any?): OpenResult {
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