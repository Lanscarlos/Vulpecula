package top.lanscarlos.vulpecula.common.applicative

import top.lanscarlos.vulpecula.common.applicative.exception.ValueConversionException
import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 20:59
 */
object IntApplicative : AbstractApplicative<Int>(Int::class.java) {

    override fun convertOrThrow(instance: Any): Int {
        return when (instance) {
            is Int -> instance
            is Number -> instance.toInt()
            is String -> instance.toIntOrNull() ?: throw ValueConversionException(instance, Int::class.java)
            else -> throw TypeConversionException(instance, Int::class.java)
        }
    }

}