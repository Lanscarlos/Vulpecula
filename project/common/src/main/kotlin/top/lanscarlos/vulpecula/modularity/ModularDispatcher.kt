package top.lanscarlos.vulpecula.modularity

import taboolib.common.platform.event.EventPriority
import taboolib.common5.Baffle
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest

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
    val listen: String

    /**
     * 是否接受已取消的事件
     * */
    val acceptCancelled: Boolean

    /**
     * 监听优先级
     * */
    val priority: EventPriority

    /**
     * 命名空间
     * */
    val namespace: List<String>

    /**
     * 预设变量
     * */
    val variables: Any?

    /**
     * 前置处理
     * */
    val preprocessor: Any?

    /**
     * 后置处理
     * */
    val postprocessor: Any?

    /**
     * 异常处理
     * */
    val exceptions: Any?

    /**
     * 用于反射获取事件中的玩家对象
     * */
    val playerReference: String?

    /**
     * 阻断器，防止频繁执行脚本
     * */
    val baffle: Baffle

    /**
     * 是否正在运行
     * */
    val isRunning: Boolean

    fun registerListener()

    fun unregisterListener()

    fun buildQuest(): BacikalQuest

}