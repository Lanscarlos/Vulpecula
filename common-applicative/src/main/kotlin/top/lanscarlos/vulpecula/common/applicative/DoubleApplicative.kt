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
object DoubleApplicative : AbstractApplicative<Double>(Double::class.java) {

    override fun convertOrThrow(instance: Any): Double {
        return when (instance) {
            is Double -> instance
            is Number -> instance.toDouble()
            is String -> instance.toDoubleOrNull() ?: throw ValueConversionException(instance, Double::class.java)
            else -> throw TypeConversionException(instance::class.java, Double::class.java)
        }
    }

}