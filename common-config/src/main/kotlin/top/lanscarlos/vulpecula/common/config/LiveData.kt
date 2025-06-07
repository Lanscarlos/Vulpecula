package top.lanscarlos.vulpecula.common.config

import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-03-10 18:58
 */
interface LiveData<out T> {

    /**
     * 键名
     * */
    val id: String

    /**
     * 获取值
     * */
    fun getValue(): T

    /**
     * 更新值
     * */
    fun update()

    /**
     * 兼容代理属性
     * */
    operator fun getValue(parent: Any?, property: KProperty<*>): T {
        return getValue()
    }

}