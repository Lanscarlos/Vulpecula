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

    /**
     * 源配置
     * */
    val config: DynamicConfig

    /**
     * 路径
     * */
    val path: String

    /**
     * 所在文件的行号
     * */
    val position: Int

    /**
     * 获取数据
     * */
    fun getValue(): T

    /**
     * 更新数据
     * */
    fun update()

    /**
     * 兼容代理属性
     * */
    operator fun getValue(source: Any?, property: KProperty<*>): T {
        return getValue()
    }
}