package top.lanscarlos.vulpecula.modularity

import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.common5.Baffle

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.modularity
 *
 * 模块化事件调度器
 *
 * @author Lanscarlos
 * @since 2023-08-28 14:43
 */
interface ModularDispatcher : ModularComponent {

    /**
     * 监听的事件类
     * */
    val listen: Class<out Event>

    /**
     * 监听优先级
     * */
    val priority: EventPriority

    /**
     * 是否接受已取消的事件
     * */
    val acceptCancelled: Boolean

    /**
     * 预设命名空间
     * */
    val namespace: List<String>

    /**
     * 前置处理
     * */
    val preprocessing: Any?

    /**
     * 后置处理
     * */
    val postprocessing: Any?

    /**
     * 处理脚本列表 (ID)
     * */
    val handlers: List<String>

    /**
     * 异常处理
     * */
    val exceptions: Any?

    /**
     * 阻断器，防止频繁执行脚本
     * */
    val baffle: Baffle?

    /**
     * 事件处理流水线
     * */
    val pipelines: List<DispatcherPipeline<*>>

    /**
     * 是否正在运行
     * */
    val isRunning: Boolean

    /**
     * 注册监听器
     * */
    fun registerListener()

    /**
     * 注销监听器
     * */
    fun unregisterListener()

}