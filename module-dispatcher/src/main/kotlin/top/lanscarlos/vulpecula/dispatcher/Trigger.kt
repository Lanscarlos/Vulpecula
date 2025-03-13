package top.lanscarlos.vulpecula.dispatcher

import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025-03-13 17:13
 */
interface Trigger {

    val clazz: Class<out Event>

    val priority: EventPriority

    val weight: Int

    fun accept(event: Event)

}