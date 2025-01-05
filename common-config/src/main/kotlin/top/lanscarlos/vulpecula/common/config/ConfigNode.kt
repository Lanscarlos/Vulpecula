package top.lanscarlos.vulpecula.common.config

import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2024-12-16 01:42
 */
interface ConfigNode<T> {

    /**
     * 路径
     * */
    val path: String

    /**
     * 获取数据
     * */
    fun getValue(): T

    /**
     * 从配置源中重载数据
     * */
    fun update()

    /**
     * 兼容代理属性
     * */
    operator fun getValue(source: Any?, property: KProperty<*>): T {
        return getValue()
    }

}