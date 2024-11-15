package top.lanscarlos.vulpecula.wireshark

import taboolib.module.nms.PacketReceiveEvent
import taboolib.module.nms.PacketSendEvent
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.wireshark
 *
 * @author Lanscarlos
 * @since 2024-11-12 19:15
 */
class DefaultPacketTracker : PacketTracker {

    val cache = LinkedList<PacketCache>()

    override fun track(event: PacketReceiveEvent) {
        cache += PacketCache(event.packet, event.player)
    }

    override fun track(event: PacketSendEvent) {
        cache += PacketCache(event.packet, event.player)
    }

}