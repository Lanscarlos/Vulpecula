package top.lanscarlos.vulpecula.wireshark

import taboolib.common.platform.function.warning
import taboolib.module.nms.PacketReceiveEvent
import taboolib.module.nms.PacketSendEvent
import java.util.LinkedList
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.wireshark
 *
 * 复合过滤器
 *
 * @author Lanscarlos
 * @since 2024-11-13 11:38
 */
class ComplexPacketFilter(var rule: Int, filters: List<PacketFilter>) : PacketFilter {

    constructor(rule: Int) : this(rule, emptyList())

    private val filters: LinkedList<PacketFilter> = LinkedList(filters)

    /**
     * 添加包名过滤规则
     *
     * @param name 过滤包名, 支持正则表达式
     */
    fun addNamedRule(name: String) {
        computeIfAbsent(NamedPacketFilter::class.java) { NamedPacketFilter() }.addRule(name)
    }

    /**
     * 添加包名过滤规则
     *
     * @param names 过滤包名, 支持正则表达式
     */
    fun addNamedRule(names: List<String>) {
        computeIfAbsent(NamedPacketFilter::class.java) { NamedPacketFilter() }.addRule(names)
    }

    /**
     * 添加正则表达式过滤规则
     *
     * @param regex 正则表达式
     */
    fun addNamedRule(regex: Regex) {
        computeIfAbsent(NamedPacketFilter::class.java) { NamedPacketFilter() }.addRule(regex)
    }

    /**
     * 添加正则表达式过滤规则
     *
     * @param regexes 正则表达式
     */
    fun addNamedRules(regexes: List<Regex>) {
        computeIfAbsent(NamedPacketFilter::class.java) { NamedPacketFilter() }.addRules(regexes)
    }

    /**
     * 设置过滤规则
     */
    fun rule(rule: Int) {
        this.rule = rule
    }

    /**
     * 添加过滤器
     */
    fun add(filter: PacketFilter) {
        filters += filter
    }

    /**
     * 添加过滤器
     */
    fun remove(filter: PacketFilter) {
        filters -= filter
    }

    /**
     * 添加过滤器
     */
    fun removeAt(index: Int) {
        filters.removeAt(index)
    }

    /**
     * 清空过滤器
     */
    fun clear() {
        filters.clear()
    }

    override fun filter(event: PacketReceiveEvent): Boolean {
        return when (rule) {
            RULE_ALL -> filters.all { it.filter(event) }
            RULE_ANY -> filters.any { it.filter(event) }
            else -> {
                warning("ComplexPacketFilter#filter >> Unknown rule: $rule. return false.")
                false
            }
        }
    }

    override fun filter(event: PacketSendEvent): Boolean {
        return when (rule) {
            RULE_ALL -> filters.all { it.filter(event) }
            RULE_ANY -> filters.any { it.filter(event) }
            else -> {
                warning("ComplexPacketFilter#filter >> Unknown rule: $rule. return false.")
                false
            }
        }
    }

    private fun <T: PacketFilter> computeIfAbsent(type: Class<T>, supplier: Supplier<T>): T {
        val filter = filters.filterIsInstance(type).firstOrNull()
        if (filter != null) {
            return filter
        }
        val newFilter = supplier.get()
        filters += newFilter
        return newFilter
    }

    companion object {

        /**
         * 全部匹配, 即所有过滤器都匹配成功才返回 true
         */
        const val RULE_ALL = 0

        /**
         * 任意匹配, 即任意一个过滤器匹配成功就返回 true
         */
        const val RULE_ANY = 1

    }
}