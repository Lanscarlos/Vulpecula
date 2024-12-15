package top.lanscarlos.vulpecula.common.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 20:59
 */
object IntApplicative : AbstractApplicative<Int>() {

    override fun transfer(instance: Any, def: Int?): Int? {
        return when (instance) {
            is Int -> instance
            is Number -> instance.toInt()
            is String -> instance.toIntOrNull()
            else -> def
        }
    }

    override fun readProperty(instance: Int, key: String): Any {
        return when (key) {
            "inc" -> instance + 1
            "dec" -> instance - 1
            "negate" -> -instance
            "pow" -> instance * instance
            "sqrt" -> kotlin.math.sqrt(instance.toDouble())
            "abs" -> if (instance < 0) -instance else instance
            "toByte" -> instance.toByte()
            "toShort" -> instance.toShort()
            "toInt" -> instance
            "toLong" -> instance.toLong()
            "toFloat" -> instance.toFloat()
            "toDouble" -> instance.toDouble()
            "toString" -> instance.toString()
            else -> failedByGetPropertyNotSupported(instance, key)
        }
    }

    override fun writeProperty(instance: Int, key: String, value: Any?) {
        failedBySetPropertyNotSupported(instance, key)
    }
}