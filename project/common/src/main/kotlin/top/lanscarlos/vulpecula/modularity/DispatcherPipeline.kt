package top.lanscarlos.vulpecula.modularity

import org.bukkit.event.Event

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.modularity
 *
 * @author Lanscarlos
 * @since 2023-12-28 00:40
 */
interface DispatcherPipeline<T: Event> {

    /**
     * 过滤事件
     * */
    fun filter(event: T): Boolean

    /**
     * 前置处理
     * */
    fun preprocess(event: T)

    /**
     * 预设内置变量
     * */
    fun variables(event: T): Map<String, Any?>

}