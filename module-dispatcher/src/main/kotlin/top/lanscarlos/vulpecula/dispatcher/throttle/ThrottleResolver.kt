package top.lanscarlos.vulpecula.dispatcher.throttle

import org.bukkit.event.Event

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.throttle
 *
 * @author Lanscarlos
 * @since 2025-03-08 22:56
 */
interface ThrottleResolver<T: Event> {

    /**
     * 解析事件并根据规则判断是否通过
     * */
    fun resolve(event: T): Boolean

    /**
     * 过滤事件
     * */
    fun filter(event: T): Boolean

    /**
     * 阻断事件
     * */
    fun baffle(event: T): Boolean

}