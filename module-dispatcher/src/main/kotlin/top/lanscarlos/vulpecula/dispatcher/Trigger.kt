package top.lanscarlos.vulpecula.dispatcher

import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.convert
import top.lanscarlos.vulpecula.common.livedata.int
import top.lanscarlos.vulpecula.common.livedata.string

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025-03-12 09:34
 */
class Trigger(config: Configuration) {

    val listenEvent: Class<*> by config.read("listen-event").string().convert {
        val clazz = Class.forName(it)
        require(Event::class.java.isAssignableFrom(clazz)) { "Event class must be subclass of class ${Event::class.java.name}" }
        clazz
    }

    val listenPriority: EventPriority by config.read("listen-priority").string("NORMAL").convert {
        val name = it.uppercase()
        require(name in EventPriority.entries.map(EventPriority::name)) { "Priority must be one of ${EventPriority.entries.map(EventPriority::name)}" }
        EventPriority.valueOf(name)
    }

    val triggerPriority: Int by config.read("rule.trigger-priority").int(8)

    fun accept(event: Event) {
        TODO("Not yet implemented")
    }

}