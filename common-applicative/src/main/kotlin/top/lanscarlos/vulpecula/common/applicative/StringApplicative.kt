package top.lanscarlos.vulpecula.common.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-11-23 15:44
 */
object StringApplicative : AbstractApplicative<String>(String::class.java) {

    override fun convert(instance: Any?): String? {
        return instance?.toString()
    }

    override fun readProperty(instance: String, key: String): Any? {
        return when (key) {
            "length" -> instance.length
            "isEmpty" -> instance.isEmpty()
            "isNotEmpty" -> instance.isNotEmpty()
            "isBlank" -> instance.isBlank()
            "isNotBlank" -> instance.isNotBlank()
            "toUpperCase" -> instance.uppercase()
            "toLowerCase" -> instance.lowercase()
            "trim" -> instance.trim()
            "trimStart" -> instance.trimStart()
            "trimEnd" -> instance.trimEnd()
            "toInt" -> instance.toIntOrNull()
            "toLong" -> instance.toLongOrNull()
            "toFloat" -> instance.toFloatOrNull()
            "toDouble" -> instance.toDoubleOrNull()
            "toBoolean" -> instance.toBoolean()
            "toString" -> instance
            else -> failedByGetPropertyNotSupported(instance, key)
        }
    }

    override fun writeProperty(instance: String, key: String, value: Any?) {
        failedBySetPropertyNotSupported(instance, key)
    }
}