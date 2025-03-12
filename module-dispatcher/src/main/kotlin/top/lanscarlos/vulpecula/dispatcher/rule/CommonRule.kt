package top.lanscarlos.vulpecula.dispatcher.rule

import org.bukkit.event.Event

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025-03-12 14:14
 */
class CommonRule(clazz: Class<out Event>) : Rule<Event> {
    override fun matches(event: Event): Boolean {
        TODO("Not yet implemented")
    }
}