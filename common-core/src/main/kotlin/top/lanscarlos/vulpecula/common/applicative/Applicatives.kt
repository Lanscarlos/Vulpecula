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

}