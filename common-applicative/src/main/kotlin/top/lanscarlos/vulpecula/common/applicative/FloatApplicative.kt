package top.lanscarlos.vulpecula.common.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 21:10
 */
object FloatApplicative : AbstractApplicative<Float>(Float::class.java) {

    override fun convert(instance: Any): Float {
        return when (instance) {
            is Float -> instance
            is Number -> instance.toFloat()
            is String -> instance.toFloatOrNull() ?: throw InvalidValueException(instance, Float::class.java)
            else -> throw UnsupportedTypeException(instance::class.java, Float::class.java)
        }
    }

    override fun readProperty(instance: Float, key: String): Any {
        return when (key) {
            "inc" -> instance + 1
            "dec" -> instance - 1
            "negate" -> -instance
            "pow" -> instance * instance
            "sqrt" -> kotlin.math.sqrt(instance.toDouble())
            "abs" -> if (instance < 0) -instance else instance
            "toByte" -> instance.toInt().toByte()
            "toShort" -> instance.toInt().toShort()
            "toInt" -> instance.toInt()
            "toLong" -> instance.toLong()
            "toFloat" -> instance
            "toDouble" -> instance.toDouble()
            "toString" -> instance.toString()
            else -> errorGetPropertyNotSupported(instance, key)
        }
    }

    override fun writeProperty(instance: Float, key: String, value: Any?) {
        errorBySetPropertyNotSupported(instance, key)
    }
}