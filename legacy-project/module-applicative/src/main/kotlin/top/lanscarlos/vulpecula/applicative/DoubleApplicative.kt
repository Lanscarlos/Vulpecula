package top.lanscarlos.vulpecula.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 21:10
 */
object DoubleApplicative : AbstractApplicative<Double>() {

    override fun transfer(instance: Any, def: Double?): Double? {
        return when (instance) {
            is Double -> instance
            is Number -> instance.toDouble()
            is String -> instance.toDoubleOrNull()
            else -> def
        }
    }

    override fun readProperty(instance: Double, key: String): Any {
        return when (key) {
            "inc" -> instance + 1
            "dec" -> instance - 1
            "negate" -> -instance
            "pow" -> instance * instance
            "sqrt" -> kotlin.math.sqrt(instance)
            "abs" -> if (instance < 0) -instance else instance
            "toByte" -> instance.toInt().toByte()
            "toShort" -> instance.toInt().toShort()
            "toInt" -> instance.toInt()
            "toLong" -> instance.toLong()
            "toFloat" -> instance.toFloat()
            "toDouble" -> instance
            "toString" -> instance.toString()
            else -> failedByGetPropertyNotSupported(instance, key)
        }
    }

    override fun writeProperty(instance: Double, key: String, value: Any?) {
        failedBySetPropertyNotSupported(instance, key)
    }
}