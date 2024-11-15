package top.lanscarlos.vulpecula.wireshark

import taboolib.module.nms.PacketReceiveEvent
import taboolib.module.nms.PacketSendEvent

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.wireshark
 *
 * @author Lanscarlos
 * @since 2024-11-12 17:52
 */
interface PacketFilter {

    /**
     * 过滤数据包
     *
     * @param event 数据包事件
     * @return 是否过滤
     */
    fun filter(event: PacketReceiveEvent): Boolean

    /**
     * 过滤数据包
     *
     * @param event 数据包事件
     * @return 是否过滤
     */
    fun filter(event: PacketSendEvent): Boolean

}