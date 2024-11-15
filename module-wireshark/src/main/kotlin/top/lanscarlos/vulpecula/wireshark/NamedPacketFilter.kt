package top.lanscarlos.vulpecula.wireshark

import taboolib.module.nms.PacketReceiveEvent
import taboolib.module.nms.PacketSendEvent
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.wireshark
 *
 * 包名过滤器, 用于针对包名的过滤, 支持正则表达式
 *
 * @author Lanscarlos
 * @since 2024-11-13 11:51
 */
class NamedPacketFilter() : PacketFilter {

    internal val names: LinkedList<String> = LinkedList()

    internal val regexes: LinkedList<Regex> = LinkedList()

    /**
     * 添加指定包名过滤规则
     *
     * @param name 过滤包名
     */
    fun addRule(name: String) {
        names += name
    }

    /**
     * 添加指定包名过滤规则
     *
     * @param names 过滤包名
     */
    fun addRule(names: List<String>) {
        this.names += names
    }

    /**
     * 添加正则表达式过滤规则
     *
     * @param regex 正则表达式
     */
    fun addRule(regex: Regex) {
        regexes += regex
    }

    /**
     * 清空规则
     */
    fun clearRules() {
        names.clear()
        regexes.clear()
    }

    /**
     * 添加正则表达式过滤规则
     *
     * @param regexes 正则表达式
     */
    fun addRule(regexes: List<Regex>) {
        this.regexes += regexes
    }

    fun filter(name: String): Boolean {
        if (names.contains(name)) {
            return true
        }
        for (regex in regexes) {
            if (name.matches(regex)) {
                return true
            }
        }
        return false
    }

    override fun filter(event: PacketReceiveEvent): Boolean {
        return filter(event.packet.name)
    }

    override fun filter(event: PacketSendEvent): Boolean {
        return filter(event.packet.name)
    }
}