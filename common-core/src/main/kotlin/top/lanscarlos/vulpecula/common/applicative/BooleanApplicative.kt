package top.lanscarlos.vulpecula.common.applicative

import top.lanscarlos.vulpecula.common.applicative.exception.ValueConversionException
import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 20:44
 */
object BooleanApplicative : AbstractApplicative<Boolean>(Boolean::class.java) {

    override val aliases: Array<String> = arrayOf("bool")

    override fun convertOrThrow(instance: Any): Boolean {
        return when (instance) {
            is Boolean -> instance
            "true", "True", "TRUE" -> true
            "false", "False", "FALSE" -> false
            is String -> throw ValueConversionException(instance, Boolean::class.java)
            else -> throw TypeConversionException(instance::class.java, Boolean::class.java)
        }
    }

}