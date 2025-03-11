package top.lanscarlos.vulpecula.dispatcher.throttle

import org.bukkit.event.Event
import taboolib.library.configuration.ConfigurationSection

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.throttle
 *
 * @author Lanscarlos
 * @since 2025-03-08 10:44
 */
class GenericThrottler<T: Event>(val clazz: Class<T>, val config: ConfigurationSection) : Throttler<T> {

    override fun throttle(event: T): Boolean {
        TODO("Not yet implemented")
    }
}