package top.lanscarlos.vulpecula.dispatcher.condition

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.condition
 *
 * @author Lanscarlos
 * @since 2025-03-10 13:46
 */
interface Condition<T> {

    /**
     * 检查输入是否符合条件
     * */
    fun check(input: T): Boolean

}