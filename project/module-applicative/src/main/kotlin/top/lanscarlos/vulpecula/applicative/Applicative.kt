package top.lanscarlos.vulpecula.applicative

import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 13:55
 */
interface Applicative<T> {

    /**
     * 取值
     * */
    fun getValue(): T?

    /**
     * 取值，如果为 null，返回默认值
     * @param def 默认值
     * */
    fun getValue(def: T): T

    /**
     * 兼容代理属性
     * */
    operator fun getValue(parent: Any?, property: KProperty<*>): T?

}