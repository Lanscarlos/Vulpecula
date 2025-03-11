package top.lanscarlos.vulpecula.common.livedata

import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.livedata
 *
 * @author Lanscarlos
 * @since 2025-03-10 18:58
 */
interface LiveData<out T> {

    fun getValue(): T

    fun getValueOrNull(): T?

    fun update()

    /**
     * 兼容代理属性
     * */
    operator fun getValue(parent: Any?, property: KProperty<*>): T {
        return getValue()
    }

}