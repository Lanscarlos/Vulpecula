package top.lanscarlos.vulpecula.internal

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.*
import taboolib.module.lang.sendLang
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-09-03 17:03
 */
class EventListener
private constructor(
    val id: String,
    val event: Class<*>,
    val priority: EventPriority
) {

    private var listener: ProxyListener? = null
    private var dispatchers = CopyOnWriteArrayList<EventDispatcher>()

    /**
     * 唤起调度器
     * */
    private fun call(event: Event) {
        // 获取事件是否被取消
        val isCancelled = (event as? Cancellable)?.isCancelled ?: false

        if (dispatchers.isEmpty()) {
            // 不存在调度器，销毁监听器
            destroy()
            return
        }

        dispatchers.forEach {
            // 过滤忽略取消事件的调度器
            if (isCancelled && it.ignoreCancelled) return@forEach
            it.run(event)
        }
    }

    fun addDispatcher(dispatcher: EventDispatcher, replace: Boolean = false) {
        if (dispatcher.id in dispatchers.map { it.id }) {
            // 已存在相同 id 的 Dispatcher
            if (!replace) return
            dispatchers.removeAll { it.id == dispatcher.id }
        }

        dispatchers += dispatcher
    }

    fun removeDispatcher(id: String) {
        dispatchers.removeAll { it.id == id }

        if (dispatchers.isEmpty()) {
            // 不存在任何调度器，销毁监听器对象
            destroy()
        }
    }

    /**
     * 注册事件监听器
     * */
    fun register() {
        if (listener != null) return
        listener = registerBukkitListener(event, priority, false) {
            if (it !is Event) return@registerBukkitListener
            call(it)
        }
    }

    /**
     * 注销监听器
     * */
    fun unregister() {
        listener?.let { unregisterListener(it) }
        listener = null
    }

    fun destroy() {
        unregister()
        cache.remove(this.id)
    }

    companion object {

        private val cache = mutableMapOf<String, EventListener>()

        fun get(eventName: String, priority: EventPriority): EventListener? {
            val key = eventName + priority.name
            return cache[key]
        }

        fun registerAll() {
            cache.values.forEach { it.register() }
        }

        fun unregisterAll() {
            cache.values.forEach { it.unregister() }
        }

        fun destroyAll() {
            cache.values.forEach { it.destroy() }
        }

        /**
         * 获取对应的监听器
         * */
        fun EventDispatcher.getListener(): EventListener? {
            val key = this.eventName + this.priority.name

            if (key in cache) return cache[key]!!

            val eventClass = try {
                Class.forName(this.eventName)
            } catch (e: Exception) {
                e.printStackTrace()
//                warning("Illegal event class: \"$this.eventName\" at dispatcher \"${this.id}\"!")
                console().sendLang("Dispatcher-Load-Failed-Event-Class-Not-Found", this.id, this.eventName)
                return null
            }

            return cache.computeIfAbsent(key) {
                EventListener(key, eventClass, this.priority)
            }
        }

    }
}