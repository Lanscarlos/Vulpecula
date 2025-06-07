package top.lanscarlos.vulpecula.common.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 13:55
 */
interface Applicative<T> {

    val name: String

    val aliases: Array<String>

    /**
     * 强制转换为目标类型
     *
     * @param instance 实例
     * @return 转换后的实例
     * @throws NullPointerException 实例为空
     * @throws top.lanscarlos.vulpecula.common.applicative.exception.InvalidValueException 内容不规范
     * @throws top.lanscarlos.vulpecula.common.applicative.exception.UnsupportedTypeException 实例类型不支持
     * */
    fun convert(instance: Any?): T

    /**
     * 转换为目标类型, 果转换失败则返回 null
     *
     * @param instance 实例
     * @return 转换后的实例
     * */
    fun convertOrNull(instance: Any?): T?

    /**
     * 获取属性
     *
     * @param key 属性名, 递归获取属性使用 . 分隔
     * @param strict 严格模式, 递归过程遇到中间属性为空或不存在时抛出异常
     * @throws IllegalStateException 如果属性不存在或者严格模式下遇到中间属性为空或不存在
     * @param reflect 是否使用反射检查预设之外的属性
     * */
    fun getProperty(instance: T, key: String, strict: Boolean = false, reflect: Boolean = false): Any?

    /**
     * 设置属性
     *
     * @param key 属性名, 递归获取属性使用 . 分隔
     * @param value 属性值
     * @param strict 严格模式, 递归过程遇到中间属性为空或不存在时抛出异常
     * @param reflect 是否使用反射检查预设之外的属性
     * @throws IllegalStateException 如果属性不存在或者严格模式下遇到中间属性为空或不存在
     * */
    fun setProperty(instance: T, key: String, value: Any?, strict: Boolean = false, reflect: Boolean = false)

}