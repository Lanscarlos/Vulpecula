package top.lanscarlos.vulpecula.dispatcher.rule

import org.bukkit.event.Event

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025-03-12 09:03
 */
interface Rule<T: Event> {

    /**
     * 匹配事件
     * */
    fun matches(event: T): Boolean

}