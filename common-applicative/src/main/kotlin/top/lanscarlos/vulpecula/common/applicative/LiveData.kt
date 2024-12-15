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
    operator fun getValue(parent: Any?, property: KProperty<*>): T? {
        return getValue()
    }

    /**
     * 读取属性
     *
     * @param key 属性名, 递归获取属性使用 . 分隔
     * @throws IllegalStateException 如果属性不存在
     * */
    operator fun get(key: String): Any?

    /**
     * 设置属性
     * */
    operator fun set(key: String, value: Any?)

}