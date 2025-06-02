package top.lanscarlos.vulpecula.common.applicative

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
            is String -> throw InvalidValueException(instance, Boolean::class.java)
            else -> throw UnsupportedTypeException(instance::class.java, Boolean::class.java)
        }
    }

    override fun readProperty(instance: Boolean, key: String): Any {
        return when (key) {
            "not" -> !instance
            "toShort" -> if (instance) 1.toShort() else 0.toShort()
            "toInt" -> if (instance) 1 else 0
            "toLong" -> if (instance) 1L else 0L
            "toFloat" -> if (instance) 1.0f else 0.0f
            "toDouble" -> if (instance) 1.0 else 0.0
            "toString" -> instance.toString()
            else -> errorGetPropertyNotSupported(instance, key)
        }
    }

    override fun writeProperty(instance: Boolean, key: String, value: Any?) {
        errorBySetPropertyNotSupported(instance, key)
    }
}