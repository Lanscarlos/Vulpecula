package top.lanscarlos.vulpecula.wireshark

import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.PacketReceiveEvent
import taboolib.module.nms.PacketSendEvent
import java.util.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.wireshark
 *
 * 发包抓包系列工具
 *
 * @author Lanscarlos
 * @since 2024-11-10 16:12
 */
object Wireshark {

    /**
     * 是否启用数据包过滤
     * */
    var isPacketFilterEnabled: Boolean = false

    /**
     * 是否启用数据包匹配
     * */
    var isPacketMatcherEnabled: Boolean = false

    /**
     * 是否启用数据包追踪
     * */
    var isPacketTrackerEnabled: Boolean = false

    /**
     * 是否启用数据包解析
     * */
    var isPacketParserEnabled: Boolean = false

    /**
     * 是否启用数据包拦截
     * */
    var isPacketInterceptorEnabled: Boolean = false

    /**
     * 是否启用数据包覆写
     * */
    var isPacketOverwriteEnabled: Boolean = false

    /**
     * 过滤器
     * */
    var filters: PacketFilter = ComplexPacketFilter(ComplexPacketFilter.RULE_ANY)

    /**
     * 匹配器
     * */
    var matcher: PacketMatcher = DefaultPacketMatcher()

    /**
     * 追踪器
     * */
    var tracker: PacketTracker = DefaultPacketTracker()

    @SubscribeEvent
    fun onPacketReceive(event: PacketReceiveEvent) {
        if (isPacketFilterEnabled && filters.filter(event)) {
            // 过滤数据包
            return
        }
        if (isPacketMatcherEnabled && !matcher.matches(event.packet)) {
            // 匹配失败
            return
        }
        if (isPacketTrackerEnabled) {
            // 追踪数据包
            tracker.track(event)
        }
        if (isPacketParserEnabled) {
            // 解析数据包
        }
        if (isPacketInterceptorEnabled) {
            // 拦截数据包
            event.isCancelled = true
            return
        }
        if (isPacketOverwriteEnabled) {
            // 覆写数据包
        }
    }

    @SubscribeEvent
    fun onPacketSend(event: PacketSendEvent) {
        if (isPacketFilterEnabled && filters.filter(event)) {
            // 过滤数据包
            return
        }
        if (isPacketMatcherEnabled && !matcher.matches(event.packet)) {
            // 匹配失败
            return
        }
        if (isPacketTrackerEnabled) {
            // 追踪数据包
            tracker.track(event)
        }
        if (isPacketParserEnabled) {
            // 解析数据包
        }
        if (isPacketInterceptorEnabled) {
            // 拦截数据包
            event.isCancelled = true
            return
        }
        if (isPacketOverwriteEnabled) {
            // 覆写数据包
        }
    }

}