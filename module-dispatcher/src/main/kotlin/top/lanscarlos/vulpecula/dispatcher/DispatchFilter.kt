package top.lanscarlos.vulpecula.dispatcher

import org.bukkit.event.Event

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025-02-04 17:24
 */
interface DispatchFilter<T: Event> {

    /**
     * 过滤事件
     *
     * @param event 事件
     * @return 是否通过
     */
    fun filter(event: T): Boolean

}