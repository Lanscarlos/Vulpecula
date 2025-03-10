package top.lanscarlos.vulpecula.common.applicative

import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 17:30
 */
interface LiveData<T> {

    /**
     * 取值, 如果为空则抛出异常
     * */
    fun getValue(): T

    /**
     * 取值, 如果为空则返回 null
     * */
    fun getValueOrNull(): T?

    /**
     * 更新数据源
     * */
    fun update(source: Any?)

    /**
     * 兼容代理属性
     * */
    operator fun getValue(parent: Any?, property: KProperty<*>): T

}