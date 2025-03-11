package top.lanscarlos.vulpecula.dispatcher.throttle

import org.bukkit.event.Event

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.throttle
 *
 * 节流器, 用于读取分析配置文件中的节流项，然后对传入的事件进行节流判定
 *
 * @author Lanscarlos
 * @since 2025-03-08 10:27
 */
interface Throttler<T: Event> {

    /**
     * 节流
     *
     * @return 是否通过
     */
    fun throttle(event: T): Boolean

}