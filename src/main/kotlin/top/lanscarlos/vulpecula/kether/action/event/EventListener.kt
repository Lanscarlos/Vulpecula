package top.lanscarlos.vulpecula.kether.action.event

import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.unregisterListener
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.event
 *
 * @author Lanscarlos
 * @since 2022-12-03 13:04
 */
class EventListener(
    val id: String,
    val event: Class<*>,
    val priority: EventPriority
) {

    private val listener = register()
    private val queue = mutableListOf<EventTask>()

    private fun call(event: Event) {
        val iterator = queue.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            task.accept(event)

            if (task.isClose()) iterator.remove()
        }

        // 队列中无任务，销毁监听器
        if (queue.isEmpty()) {
            destroy()
        }
    }

    fun pushTask(task: EventTask) {
        if (task in queue) queue.remove(task)
        queue.add(task)
    }

    /**
     * 注册事件监听器
     * */
    private fun register(): ProxyListener {
        return registerBukkitListener(event, priority, false) {
            if (it !is Event) return@registerBukkitListener
            call(it)
        }
    }

    /**
     * 注销监听器
     * */
    private fun unregister() {
        unregisterListener(listener)
    }

    /**
     * 销毁监听器
     * */
    fun destroy() {
        unregister()
        cache.remove(this.id)
    }

    companion object {

        private val cache = mutableMapOf<String, EventListener>()

        internal fun registerTask(eventName: String, priority: EventPriority, id: String, func: EventTask.(Event) -> Unit) {
            registerTask(eventName, priority, object : EventTask(id) {
                override fun accept(event: Event) {
                    func(event)
                }
            })
        }

        fun registerTask(eventName: String, priority: EventPriority, task: EventTask) {
            val key = eventName + priority.name
            if (key in cache) {
                cache[key]!!.pushTask(task)
                return
            }

            try {
                val eventClass = Class.forName(eventName)
                val listener = cache.computeIfAbsent(key) {
                    EventListener(key, eventClass, priority)
                }
                listener.pushTask(task)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    abstract class EventTask(val id: String) : AutoCloseable, Consumer<Event> {

        private var closed = false

        fun isClose(): Boolean {
            return closed
        }

        override fun close() {
            closed = true
        }
    }
}