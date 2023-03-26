package top.lanscarlos.vulpecula.bacikal.action.event

import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.common5.cbool
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.internal.EventListener
import top.lanscarlos.vulpecula.utils.setVariable
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.event
 *
 * @author Lanscarlos
 * @since 2023-03-23 16:20
 */
object ActionEventWait : ActionEvent.Resolver {

    override val name: Array<String> = arrayOf("wait", "require")

    /**
     * event wait
     * */
    override fun resolve(reader: ActionEvent.Reader): ActionEvent.Handler<out Any?> {
        return reader.handle {
            combineOf(
                text(display = "event name"),
                argument("priority", "p", then = text("NORMAL"), def = "NORMAL"),
                argument("ignored-cancelled", "ignored", "ic", then = bool(true), def = true),
                argument("unique", "id", then = text(display = "unique"), def = UUID.randomUUID().toString()),
                argument("filter", "condition", then = action()),
                argument("async", "a", then = LiveData.point(true), def = false),
                argument("timeout", "time", "t", then = long(-1L), def = -1L),
                argument("onTimeout", then = action()),
                optional("then", then = action())
            ) { eventName, priority, ignoredCancelled, taskId, filter, async, timeout, onTimeout, body ->
                val future = CompletableFuture<Any?>()

                // 异步执行
                if (async) {
                    future.complete(null)
                }

                // 设置超时
                if (timeout > 0) {
                    submit(delay = timeout) {

                        val completed = this@combineOf.variables().get<Boolean>("@Completed").let {
                            if (it.isPresent) it.get() else false
                        }

                        // 已完成
                        if (completed) {
                            return@submit
                        }

                        // 设置变量通信
                        this@combineOf.variables().set("@Timeout", true)

                        // 注销事件监听任务
                        EventListener.unregisterTask(taskId)

                        // 执行超时处理
                        if (onTimeout != null) {
                            this@combineOf.newFrame(onTimeout).run<Any?>().thenAccept {
                                if (!future.isDone) {
                                    // future 可能被 async 提前完成
                                    future.complete(it)
                                }
                            }
                        } else {
                            if (!future.isDone) {
                                // future 可能被 async 提前完成
                                future.complete(null)
                            }
                        }
                    }
                }

                // 注册事件监听器
                EventListener.registerTask(eventName, priority.asEventPriority(), ignoredCancelled, taskId) { event ->

                    val timedOut = this@combineOf.variables().get<Boolean>("@Timeout").let {
                        if (it.isPresent) it.get() else false
                    }

                    // 已超时
                    if (timedOut) {
                        // 关闭事件监听器
                        this.close()
                        return@registerTask
                    }

                    if (filter != null) {
                        val newFrame = this@combineOf.newFrame(filter)
                        newFrame.variables().set("@Event", event)
                        newFrame.variables().set("event", event)
                        val filtered = newFrame.run<Any?>().getNow(true).cbool
                        // 条件过滤不通过，等待下一次接收事件
                        if (!filtered) return@registerTask
                    }

                    // 关闭事件监听器
                    this.close()

                    // 设置变量通信
                    this@combineOf.variables().set("@Completed", true)

                    if (body != null) {
                        val newFrame = this@combineOf.newFrame(body)
                        newFrame.variables().set("@Event", event)
                        newFrame.variables().set("event", event)
                        newFrame.run<Any?>().thenAccept {
                            if (!future.isDone) {
                                // future 可能被 async 提前完成
                                future.complete(it)
                            }
                        }
                    } else {
                        if (!future.isDone) {
                            // future 可能被 async 提前完成
                            future.complete(null)
                        }
                    }
                }

                future
            }
        }
    }

    private fun String.asEventPriority(): EventPriority {
        return EventPriority.values().firstOrNull { it.name.equals(this, true) } ?: EventPriority.NORMAL
    }
}