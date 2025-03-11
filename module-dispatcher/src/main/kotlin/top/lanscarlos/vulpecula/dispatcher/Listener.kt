package top.lanscarlos.vulpecula.dispatcher

import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.unregisterListener
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025-03-09 20:04
 */
class Listener(val clazz: Class<out Event>, val priority: EventPriority) {

    val uniqueId = clazz.name + priority.name

    val handlers = LinkedList<Consumer<in Event>>()

    val listener = registerBukkitListener(clazz, priority, false) {
        accept(it)
    }

    fun register(consumer: Consumer<in Event>) {
        handlers += consumer
    }

    fun accept(event: Event) {
        for (handler in handlers) {
            handler.accept(event)
        }
    }

    fun dispose() {
        unregisterListener(listener)
    }

    companion object {

        private val listeners = ConcurrentHashMap<String, Listener>()

        @Suppress("UNCHECKED_CAST")
        fun <T: Event> register(clazz: Class<T>, priority: EventPriority, func: Consumer<T>) {
            val listener = listeners.computeIfAbsent(clazz.name + priority.name) {
                Listener(clazz, priority)
            }
            listener.register(func as Consumer<in Event>)
        }

        fun unregister(clazz: Class<out Event>, priority: EventPriority) {
            listeners.remove(clazz.name + priority.name)?.dispose()
        }

    }

}