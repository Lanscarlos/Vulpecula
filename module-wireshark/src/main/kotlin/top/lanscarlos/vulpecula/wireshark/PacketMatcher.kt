package top.lanscarlos.vulpecula.wireshark

import taboolib.module.nms.Packet

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.wireshark
 *
 * @author Lanscarlos
 * @since 2024-11-12 21:48
 */
interface PacketMatcher {

    fun matches(packet: Packet): Boolean

}