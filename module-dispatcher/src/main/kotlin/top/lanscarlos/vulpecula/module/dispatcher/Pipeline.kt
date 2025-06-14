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
     * 初始化玩家对象
     * */
    fun initPlayer(context: Context)

    /**
     * 初始化变量
     * */
    fun initVariables(context: Context)

    /**
     * 对事件进行过滤或阻断处理, 处理结果直接填在 context 里
     * */
    fun filter(context: Context)

    /**
     * 后置处理
     * */
    fun postprocess(context: Context)

}