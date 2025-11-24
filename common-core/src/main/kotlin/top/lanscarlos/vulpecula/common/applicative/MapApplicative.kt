package top.lanscarlos.vulpecula.common.applicative

import taboolib.library.configuration.ConfigurationSection
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
            is ConfigurationSection -> instance.getValues(false)
            is org.bukkit.configuration.ConfigurationSection -> instance.getValues(false)
            else -> throw TypeConversionException(instance, Map::class.java)
        }
    }

}