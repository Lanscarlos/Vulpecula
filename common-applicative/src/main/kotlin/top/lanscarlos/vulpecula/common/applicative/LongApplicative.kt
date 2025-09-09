package top.lanscarlos.vulpecula.common.applicative

import top.lanscarlos.vulpecula.common.applicative.exception.ValueConversionException
import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 21:09
 */
object LongApplicative : AbstractApplicative<Long>(Long::class.java) {

    override fun convertOrThrow(instance: Any): Long {
        return when (instance) {
            is Long -> instance
            is Number -> instance.toLong()
            is String -> instance.toLongOrNull() ?: throw ValueConversionException(instance, Long::class.java)
            else -> throw TypeConversionException(instance::class.java, Long::class.java)
        }
    }

}