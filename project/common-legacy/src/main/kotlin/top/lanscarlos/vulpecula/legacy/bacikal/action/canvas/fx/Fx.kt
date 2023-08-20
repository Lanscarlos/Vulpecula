package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.fx

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * @author Lanscarlos
 * @since 2023-06-29 11:36
 */
interface Fx<in T, out R> {

    /**
     * 根据自增计算输出值
     * */
    fun calculate(): R

    /**
     * 根据输入值计算输出值
     * */
    fun calculate(input: T): R

    fun copy(): Fx<T, R>
}