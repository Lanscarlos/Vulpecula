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
     * @throws top.lanscarlos.vulpecula.common.applicative.exception.ValueConversionException 内容不规范
     * @throws top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException 实例类型不支持
     * */
    fun convert(instance: Any?): T

    /**
     * 转换为目标类型, 果转换失败则返回 null
     *
     * @param instance 实例
     * @return 转换后的实例
     * */
    fun convertOrNull(instance: Any?): T?

}