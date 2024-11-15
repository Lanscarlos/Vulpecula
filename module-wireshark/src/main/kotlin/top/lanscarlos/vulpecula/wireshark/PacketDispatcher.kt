package top.lanscarlos.vulpecula.wireshark

import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.PacketReceiveEvent
import taboolib.module.nms.PacketSendEvent

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.wireshark
 *
 * @author Lanscarlos
 * @since 2024-11-12 15:59
 */
object PacketDispatcher {

    @SubscribeEvent
    fun onPacketReceive(e: PacketReceiveEvent) {
        for (session in Session.registry.values) {
            session.onPacketReceive(e)
        }
    }

    @SubscribeEvent
    fun onPacketSend(e: PacketSendEvent) {
        for (session in Session.registry.values) {
            session.onPacketSend(e)
        }
    }

}