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
            if (trigger.priority != priority) {
                continue
            }
            trigger.accept(event)
        }
    }

    fun register(trigger: Trigger) {
        val priority = trigger.priority

        // 添加处理器
        triggers += trigger
        // 按优先级排序, 优先级越高越先处理
        triggers.sortByDescending { it.weight }

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
        val group = triggers.groupBy { it.priority }
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

        private val related = ConcurrentHashMap<Trigger, Class<out Event>>()

        fun register(trigger: Trigger) {
            val listener = cache.computeIfAbsent(trigger.clazz) { Listener(trigger.clazz) }
            related[trigger] = trigger.clazz
            listener.register(trigger)
        }

        fun update(trigger: Trigger) {
            val clazz = related[trigger] ?: return
            cache[clazz]?.unregister(trigger)
            register(trigger)
        }

        fun unregister(trigger: Trigger) {
            val clazz = related.remove(trigger) ?: return
            cache[clazz]?.unregister(trigger)
        }

    }

}