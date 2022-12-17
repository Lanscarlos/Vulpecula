package top.lanscarlos.vulpecula.internal

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.unregisterListener
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
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
    val mapper: String,
    val priority: EventPriority
) {

    private lateinit var listener: ProxyListener

    private val queue = mutableListOf<String>()

    private fun call(event: Event) {
        val isCancelled = (event as? Cancellable)?.isCancelled ?: false

        val iterator = queue.iterator()
        while (iterator.hasNext()) {
            val task = tasks[iterator.next()]
            if (task == null) {
                iterator.remove()
                continue
            }

            if (isCancelled && task.ignoreCancelled) continue

            task.accept(event)

            if (task.isClose()) {
                iterator.remove()
                tasks.remove(task.id)
            }
        }

        // 队列中无任务，销毁监听器
        if (queue.isEmpty()) {
            destroyListener()
        }
    }

    fun pushTask(taskId: String) {
        if (taskId in queue) return
        queue.add(taskId)

        if (!hasRegistered()) registerListener()
    }

    fun removeTask(taskId: String) {
        queue.remove(taskId)

        // 队列中无任务，销毁监听器
        if (queue.isEmpty()) {
            destroyListener()
        }
    }

    /**
     * 判断事件监听器是否已注册
     * */
    fun hasRegistered(): Boolean {
        return ::listener.isInitialized
    }

    /**
     * 注册事件监听器
     * */
    fun registerListener() {
        EventMapper.mapping(this)?.let { eventClass ->
            // 取消原先注册的监听器
            unregisterListener()

            listener = registerBukkitListener(eventClass, priority, false) {
                if (it !is Event) return@registerBukkitListener
                call(it)
            }
        }
    }

    /**
     * 注销监听器
     * */
    fun unregisterListener() {
        if (hasRegistered()) unregisterListener(listener)
    }

    /**
     * 销毁监听器
     * */
    fun destroyListener() {
        unregisterListener()
        cache.remove(this.id)
    }

    companion object {

        val tasks = ConcurrentHashMap<String, EventTask>()
        private val cache = mutableMapOf<String, EventListener>()

        /**
         * 注册任务
         *
         * @param mapper 事件类名或别名（映射名）
         * @param priority 事件监听等级
         * @param ignoreCancelled 是否忽略已取消的事件
         * @param taskId 任务 ID
         * @param func 事件发生时的处理
         * */
        internal fun registerTask(mapper: String, priority: EventPriority, ignoreCancelled: Boolean, taskId: String, func: EventTask.(Event) -> Unit) {
            registerTask(mapper, priority, object : EventTask(taskId, ignoreCancelled) {
                override fun accept(event: Event) {
                    func(event)
                }
            })
        }

        /**
         * 注册任务
         *
         * @param mapper 事件类名或别名（映射名）
         * @param priority 事件监听等级
         * @param task 任务对象
         * */
        fun registerTask(mapper: String, priority: EventPriority, task: EventTask) {
            val key = mapper + priority.name

            tasks[task.id] = task

            cache.computeIfAbsent(key) {
                EventListener(key, mapper, priority)
            }.pushTask(task.id)
        }

        /**
         * 注销任务
         *
         * @param taskId 任务 ID
         * */
        fun unregisterTask(taskId: String) {
            if (!tasks.containsKey(taskId)) return
            tasks.remove(taskId)
            cache.values.forEach { it.removeTask(taskId) }
        }

        /**
         * 注销所有监听器及其任务
         * */
        fun unregisterAll() {
            tasks.clear()
            cache.values.forEach { it.unregisterListener() }
            cache.clear()
        }
    }

    abstract class EventTask(
        val id: String,
        val ignoreCancelled: Boolean = true
    ) : Closeable, Consumer<Event> {

        private var closed = false

        fun isClose(): Boolean {
            return closed
        }

        override fun close() {
            closed = true
        }
    }
}