package top.lanscarlos.vulpecula.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 13:55
 */
interface Applicative<T> {

    /**
     * 转换为目标类型
     * */
    fun apply(instance: Any): T?

    /**
     * 转换为目标类型, 如果转换失败则返回默认值
     *
     * @param def 默认值
     * */
    fun apply(instance: Any, def: T): T

    /**
     * 接收实例并转换为对应的 LiveData
     * */
    fun accept(instance: Any): LiveData<T>

    /**
     * 获取属性
     *
     * @param key 属性名, 递归获取属性使用 . 分隔
     * @param strict 严格模式, 递归过程遇到中间属性为空或不存在时抛出异常
     * @throws IllegalStateException 如果属性不存在或者严格模式下遇到中间属性为空或不存在
     * */
    fun getProperty(instance: T, key: String, strict: Boolean = false): Any?

    /**
     * 设置属性
     *
     * @param key 属性名, 递归获取属性使用 . 分隔
     * @param value 属性值
     * @param strict 严格模式, 递归过程遇到中间属性为空或不存在时抛出异常
     * @throws IllegalStateException 如果属性不存在或者严格模式下遇到中间属性为空或不存在
     * */
    fun setProperty(instance: T, key: String, value: Any?, strict: Boolean = false)

}