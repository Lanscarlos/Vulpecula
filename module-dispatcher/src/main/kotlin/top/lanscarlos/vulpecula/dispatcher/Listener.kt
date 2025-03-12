package top.lanscarlos.vulpecula.dispatcher

import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.unregisterListener
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025-03-09 20:04
 */
class Listener(val clazz: Class<out Event>) {

    val triggers: LinkedList<Trigger> = LinkedList()

    val listeners: EnumMap<EventPriority, ProxyListener> = EnumMap(EventPriority::class.java)

    private fun accept(priority: EventPriority, event: Event) {
        for (trigger in triggers) {
            if (trigger.listenPriority != priority) {
                continue
            }
            trigger.accept(event)
        }
    }

    fun register(trigger: Trigger) {
        val priority = trigger.listenPriority

        // 添加处理器
        triggers += trigger
        // 按优先级排序, 优先级越高越先处理
        triggers.sortByDescending { it.triggerPriority }

        // 检查并注册监听器
        listeners.computeIfAbsent(priority) {
            registerBukkitListener(clazz, priority, false) { event ->
                accept(priority, event)
            }
        }
    }

    fun unregister(trigger: Trigger) {
        triggers.remove(trigger)

        // 检查是否还有监听器
        if (triggers.isEmpty()) {
            dispose()
            return
        }

        // 检查不同优先级是否还有触发器
        val group = triggers.groupBy { it.listenPriority }
        for (priority in EventPriority.entries) {
            if (group.getOrDefault(priority, emptyList()).isNotEmpty()) {
                continue
            }
            // 注销监听器
            listeners.remove(priority)?.let(::unregisterListener)
        }
    }

    /**
     * 注销监听器
     * */
    fun dispose() {
        for (listener in listeners.values) {
            unregisterListener(listener)
        }
        cache.remove(clazz)
    }

    companion object {

        private val cache = ConcurrentHashMap<Class<out Event>, Listener>()

        fun <T: Event> register(clazz: Class<T>, trigger: Trigger) {
            val listener = cache.computeIfAbsent(clazz) { Listener(clazz) }
            listener.register(trigger)
        }

        fun unregister(trigger: Trigger) {
            cache[trigger.listenEvent]?.unregister(trigger)
        }

    }

}