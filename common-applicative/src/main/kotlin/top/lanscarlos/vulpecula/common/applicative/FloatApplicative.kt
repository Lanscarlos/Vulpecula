package top.lanscarlos.vulpecula.common.applicative

import top.lanscarlos.vulpecula.common.applicative.exception.ValueConversionException
import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 21:10
 */
object FloatApplicative : AbstractApplicative<Float>(Float::class.java) {

    override fun convertOrThrow(instance: Any): Float {
        return when (instance) {
            is Float -> instance
            is Number -> instance.toFloat()
            is String -> instance.toFloatOrNull() ?: throw ValueConversionException(instance, Float::class.java)
            else -> throw TypeConversionException(instance::class.java, Float::class.java)
        }
    }

}