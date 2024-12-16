package top.lanscarlos.vulpecula.common.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 21:09
 */
object LongApplicative : AbstractApplicative<Long>() {

    override fun apply(instance: Any?): Long? {
        return when (instance) {
            is Long -> instance
            is Number -> instance.toLong()
            is String -> instance.toLongOrNull()
            else -> null
        }
    }

    override fun readProperty(instance: Long, key: String): Any {
        return when (key) {
            "inc" -> instance + 1
            "dec" -> instance - 1
            "negate" -> -instance
            "pow" -> instance * instance
            "sqrt" -> kotlin.math.sqrt(instance.toDouble())
            "abs" -> if (instance < 0) -instance else instance
            "toByte" -> instance.toByte()
            "toShort" -> instance.toShort()
            "toInt" -> instance.toInt()
            "toLong" -> instance
            "toFloat" -> instance.toFloat()
            "toDouble" -> instance.toDouble()
            "toString" -> instance.toString()
            else -> failedByGetPropertyNotSupported(instance, key)
        }
    }

    override fun writeProperty(instance: Long, key: String, value: Any?) {
        failedBySetPropertyNotSupported(instance, key)
    }
}