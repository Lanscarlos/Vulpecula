package top.lanscarlos.vulpecula.dispatcher

import org.bukkit.entity.Player
import org.bukkit.event.Event

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025-03-12 17:47
 */
class Context(private val event: Event, var player: Player?) {

    @Suppress("UNCHECKED_CAST")
    fun <T: Event> event(): T {
        return event as T
    }

}