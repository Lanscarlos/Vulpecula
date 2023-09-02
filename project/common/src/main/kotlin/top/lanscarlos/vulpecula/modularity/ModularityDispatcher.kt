package top.lanscarlos.vulpecula.modularity

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
interface ModularityDispatcher {

    val id: String

    /**
     * 绑定的事件类
     * */
    val bind: String

    /**
     * 是否接受已取消的事件
     * */
    val acceptCancelled: Boolean

    /**
     * 优先级，越高越优先
     * */
    val priority: Int

    /**
     * 用于反射获取事件中的玩家对象
     * */
    val playerReference: String

    /**
     * 阻断器，防止频繁执行脚本
     * */
    val baffle: Baffle

    /**
     * 是否已禁用
     * */
    val isDisabled: Boolean

}