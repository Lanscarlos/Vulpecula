package top.lanscarlos.vulpecula.common.applicative

import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2025-03-10 19:29
 */
object MapApplicative : AbstractApplicative<Map<*, *>>(Map::class.java) {

    override fun convertOrThrow(instance: Any): Map<*, *> {
        return when (instance) {
            is Map<*, *> -> instance
            is taboolib.library.configuration.ConfigurationSection -> instance.getValues(false)
            is org.bukkit.configuration.ConfigurationSection -> instance.getValues(false)
            else -> throw TypeConversionException(instance::class.java, Map::class.java)
        }
    }

    override fun readProperty(instance: Map<*, *>, key: String): Any? {
        return when (key) {
            "size" -> instance.size
            "isEmpty", "empty" -> instance.isEmpty()
            "isNotEmpty", "notEmpty" -> instance.isNotEmpty()
            "keys" -> instance.keys
            "values" -> instance.values
            "entries" -> instance.entries
            else -> instance[key]
        }
    }

    override fun writeProperty(instance: Map<*, *>, key: String, value: Any?) {
        errorBySetPropertyNotSupported(instance, key)
    }
}