package top.lanscarlos.vulpecula.common.config

import java.util.function.Consumer
import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-03-10 18:58
 */
interface LiveData<T> {

    /**
     * 键名
     * */
    val id: String

    /**
     * 获取值
     * */
    fun getValue(): T

    /**
     * 值更新时调用
     * */
    fun onUpdate(func: Consumer<T>)

    /**
     * 兼容代理属性
     * */
    operator fun getValue(parent: Any?, property: KProperty<*>): T {
        return getValue()
    }

}