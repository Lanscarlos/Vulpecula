package top.lanscarlos.vulpecula.module.dispatcher

import org.bukkit.event.Event

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/12 8:57
 */
interface Pipeline<T: Event> {

    val priority: Int

    /**
     * 对事件进行处理, 处理结果需要置于 context 中
     * */
    fun process(context: Context)

    /**
     * 对事件进行前置处理, 并做一定的解析操作等等
     * */
    fun preprocess(context: Context)

    /**
     * 后置处理
     * */
    fun postprocess(context: Context)

}