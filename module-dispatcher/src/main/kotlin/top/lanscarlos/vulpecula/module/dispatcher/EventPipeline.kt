package top.lanscarlos.vulpecula.module.dispatcher

import org.bukkit.event.Event

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/12 8:57
 */
interface EventPipeline<T: Event> {

    /**
     * 对事件进行处理, 并做一定的解析操作等等
     * */
    fun process(context: Context)

}