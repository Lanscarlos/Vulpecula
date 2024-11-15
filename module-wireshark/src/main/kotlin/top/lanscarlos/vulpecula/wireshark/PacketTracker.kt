package top.lanscarlos.vulpecula.wireshark

import taboolib.module.nms.PacketReceiveEvent
import taboolib.module.nms.PacketSendEvent

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.wireshark
 *
 * @author Lanscarlos
 * @since 2024-11-12 19:14
 */
interface PacketTracker {

    fun track(event: PacketReceiveEvent)

    fun track(event: PacketSendEvent)

}