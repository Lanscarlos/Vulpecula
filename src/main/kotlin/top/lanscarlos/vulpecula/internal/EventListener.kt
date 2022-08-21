package top.lanscarlos.vulpecula.internal

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.unregisterListener
import taboolib.common.platform.function.warning
import top.lanscarlos.vulpecula.utils.Debug
import top.lanscarlos.vulpecula.utils.Debug.debug

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-08-19 13:42
 */
class EventListener
private constructor(
    val id: String,
    val event: Class<*>,
    val priority: EventPriority
) {

    private var listener: ProxyListener? = null
    private var dispatchers = mutableListOf<EventDispatcher>()

    /**
     * 唤起调度器
     * */
    fun call(event: Event) {
        // 获取事件是否被取消
        val isCancelled = (event as? Cancellable)?.isCancelled ?: false

        if (dispatchers.isEmpty()) {
            // 不存在调度器
            unregister()
            cache.remove(id)
            return
        }

        dispatchers.forEach {
            // 过滤忽略取消事件的调度器
            if (isCancelled && it.ignoreCancelled) return@forEach
            it.run(event)
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

    companion object {

        private val cache = mutableMapOf<String, EventListener>()

        /**
         * 注册所有监听模块
         * */
        fun registerAll() {
            cache.values.forEach { it.register() }
        }

        /**
         * 注销所有监听模块
         * */
        fun unregisterAll() {
            cache.values.forEach {
                it.unregister()
                it.dispatchers.clear()
            }
            cache.clear()
        }

        fun registerDispatcher(dispatcher: EventDispatcher, eventName: String, priority: EventPriority, register: Boolean = false) {

            val event = try {
                Class.forName(eventName)
            } catch (e: Exception) {
                warning("Illegal event class: \"$eventName\" at dispatcher \"${dispatcher.id}\"!")
                e.printStackTrace()
                return
            }

            val key = eventName + priority
            val listener = cache.computeIfAbsent(key) { EventListener(key, event, priority) }
            listener.dispatchers += dispatcher

            if (register) {
                listener.register()
            }
        }

        fun unregisterDispatcher(dispatcher: EventDispatcher) {
            debug(Debug.HIGH, "尝试注销调度器 ${dispatcher.id}")

            val listener = cache.values.firstOrNull { dispatcher in it.dispatchers } ?: return

            debug("注销调度器 ${dispatcher.id} -> 监听器 id ${listener.id}")

            listener.dispatchers -= dispatcher

            // 无调度器
            if (listener.dispatchers.isEmpty()) {
                debug("注销调度器: ${dispatcher.id} -> 监听器内无调度器")
                // 注销事件监听并移除缓存
                listener.unregister()
                cache.remove(listener.id)
            } else {
                debug(Debug.HIGH, "注销调度器: ${dispatcher.id} -> 监听器内有剩余调度器")
            }
        }
    }

}