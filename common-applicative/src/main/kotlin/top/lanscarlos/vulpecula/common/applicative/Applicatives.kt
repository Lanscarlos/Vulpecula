package top.lanscarlos.vulpecula.common.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2025-02-27 17:46
 */
object Applicatives {

    /**
     * 转换为目标类型, 果转换失败则返回 null
     *
     * @param instance 实例
     * @return 转换后的实例
     * */
    inline fun <reified T: Any> convert(instance: Any?): T? {
        val applicative = ApplicativeRegistry.getApplicative(T::class.java)
        return applicative.convertOrNull(instance)
    }

    /**
     * 强制转换为目标类型
     *
     * @param instance 实例
     * @return 转换后的实例
     * @throws IllegalStateException 如果转换失败
     * */
    inline fun <reified T: Any> convertOrThrow(instance: Any?): T {
        val applicative = ApplicativeRegistry.getApplicative(T::class.java)
        return applicative.convert(instance)
    }

    /**
     * 获取属性
     *
     * @param key 属性名, 递归获取属性使用 . 分隔
     * @param strict 严格模式, 递归过程遇到中间属性为空或不存在时抛出异常
     * @param reflect 是否使用反射检查预设之外的属性
     * @throws IllegalStateException 如果属性不存在或者严格模式下遇到中间属性为空或不存在
     * */
    fun <T: Any> readProperty(instance: T, key: String, strict: Boolean, reflect: Boolean): Any? {
        val applicative = ApplicativeRegistry.getApplicative(instance.javaClass)
        return applicative.getProperty(instance, key, strict, reflect)
    }

    /**
     * 设置属性
     *
     * @param key 属性名, 递归获取属性使用 . 分隔
     * @param value 属性值
     * @param strict 严格模式, 递归过程遇到中间属性为空或不存在时抛出异常
     * @param reflect 是否使用反射检查预设之外的属性
     * @throws IllegalStateException 如果属性不存在或者严格模式下遇到中间属性为空或不存在
     * */
    fun <T: Any> writeProperty(instance: T, key: String, value: Any?, strict: Boolean, reflect: Boolean) {
        val applicative = ApplicativeRegistry.getApplicative(instance.javaClass)
        applicative.setProperty(instance, key, value, strict, reflect)
    }

}