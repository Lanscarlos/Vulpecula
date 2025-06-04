package top.lanscarlos.vulpecula.dispatcher.condition

import top.lanscarlos.vulpecula.dispatcher.Context

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.condition
 *
 * @author Lanscarlos
 * @since 2025-03-10 13:46
 */
interface Condition {

    /**
     * 检查输入是否符合条件
     * */
    fun check(context: Context): Boolean

}