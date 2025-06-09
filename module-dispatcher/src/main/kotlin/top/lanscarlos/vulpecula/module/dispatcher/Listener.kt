package top.lanscarlos.vulpecula.module.dispatcher

import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.registerBukkitListener
import taboolib.library.reflex.ReflexClass
import java.io.Closeable
import java.util.EnumMap
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/9
 */
class Listener(val clazz: ReflexClass) {

    val dispatchers: EnumMap<EventPriority, LinkedList<Dispatcher>> = EnumMap(EventPriority::class.java)

    val listeners: EnumMap<EventPriority, ProxyListener> = EnumMap(EventPriority::class.java)

    fun register(dispatcher: Dispatcher) {
        val priority = dispatcher.priority

        val dispatchers = this.dispatchers.computeIfAbsent(priority) { LinkedList() }
        dispatchers += dispatcher
        dispatchers.sortByDescending { it.weight } // 按权重排序

        listeners.computeIfAbsent(priority) {
            registerBukkitListener(clazz.toClass(), priority, false) { event ->
                accept(priority, event as Event)
            }
        }
    }

    fun unregister(dispatcher: Dispatcher) {
        dispatchers[dispatcher.priority]?.remove(dispatcher)
    }

    fun accept(priority: EventPriority, event: Event) {
        val dispatchers = this.dispatchers[priority] ?: return
        for (dispatcher in dispatchers) {
            dispatcher.accept(event)
        }
    }

    companion object {

        private val cache = ConcurrentHashMap<ReflexClass, Listener>()

        fun register(dispatcher: Dispatcher) {
            val clazz = dispatcher.clazz
            val listener = cache.computeIfAbsent(clazz) { Listener(clazz) }
            listener.register(dispatcher)
        }

        fun unregister(dispatcher: Dispatcher) {
            cache[dispatcher.clazz]?.unregister(dispatcher)
        }

    }

}