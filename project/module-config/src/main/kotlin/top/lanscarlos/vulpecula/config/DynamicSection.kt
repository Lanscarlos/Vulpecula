package top.lanscarlos.vulpecula.config

import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2023-08-25 00:30
 */
interface DynamicSection<T> {

    val parent: DynamicConfig

    /**
     * 路径
     * */
    val path: String

    /**
     * 获取数据
     * */
    fun getValue(): T

    /**
     * 更新数据
     * */
    fun update()

    operator fun getValue(source: Any?, property: KProperty<*>): T {
        return getValue()
    }
}