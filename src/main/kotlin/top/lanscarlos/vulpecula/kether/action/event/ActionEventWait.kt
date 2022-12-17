package top.lanscarlos.vulpecula.kether.action.event

import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.warning
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.internal.EventListener
import top.lanscarlos.vulpecula.internal.EventMapper
import top.lanscarlos.vulpecula.kether.action.ActionBlock
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.readBoolean
import top.lanscarlos.vulpecula.kether.live.readInt
import top.lanscarlos.vulpecula.kether.live.readString
import top.lanscarlos.vulpecula.utils.*
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.event
 *
 * @author Lanscarlos
 * @since 2022-12-03 12:27
 */
class ActionEventWait(
    val eventName: LiveData<String>
) : ScriptAction<Any?>() {

    /* 事件处理语句 */
    val actions = mutableListOf<ParsedAction<*>>()

    /* 事件任务 ID */
    var taskId: LiveData<String>? = null

    /* 事件监听等级 */
    var priority: LiveData<String>? = null

    /* 事件监听等级 */
    var ignoredCancelled: LiveData<Boolean>? = null

    /* 事件过滤条件 */
    var condition: LiveData<Boolean>? = null

    /* 是否异步监听 */
    var async: LiveData<Boolean>? = null

    /* 超时时间 */
    var timeout: LiveData<Int>? = null

    /* 超时处理语句 */
    var onTimeout: ParsedAction<*>? = null

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        val future = CompletableFuture<Any?>()
        val body = if (actions.isNotEmpty()) ParsedAction(ActionBlock(actions)) else null

        listOf(
            eventName.getOrNull(frame),
            priority?.getOrNull(frame),
            ignoredCancelled?.getOrNull(frame),
            taskId?.getOrNull(frame),
            async?.getOrNull(frame),
            timeout?.getOrNull(frame),
        ).thenTake().thenAccept { args ->

            val mapper = args[0]?.toString() ?: let {
                future.complete(null)
                warning("Cannot get listen event mapping: \"${args[0]}\"")
                return@thenAccept
            }

            val priority = args[1]?.toString()?.let { name ->
                EventPriority.values().firstOrNull { it.name.equals(name, true) }
            } ?: EventPriority.NORMAL

            val ignoredCancelled = args[2].coerceBoolean(true)

            // 获取任务 ID
            val taskId = args[3]?.toString() ?: UUID.randomUUID().toString()

            if (args[4].coerceBoolean(false)) {
                // 异步执行
                future.complete(null)
            }

            val timeout = args[5].coerceInt(-1)
            if (timeout > 0) {
                // 设置超时
                submit(delay = timeout.toLong()) {
                    if (future.isDone) return@submit

                    // 设置变量通信
                    frame.setVariable("@Timeout", true)

                    // 注销事件任务
                    EventListener.unregisterTask(taskId)

                    // 超时处理
                    onTimeout?.let { action ->
                        frame.run(action).thenAccept { future.complete(it) }
                    } ?: future.complete(null)
                }
            }

            EventListener.registerTask(mapper, priority, ignoredCancelled, taskId) { event ->

                // 检测是否超时
                if (frame.variables().get<Boolean>("@Timeout").let { if (it.isPresent) it.get() else false }) {
                    // 已超时
                    return@registerTask
                }

                frame.variables().set("@Event", event)
                frame.variables().set("event", event)

                tryAcceptEvent(frame, condition) {
                    // 关闭任务
                    this.close()

                    if (body != null) {
                        if (future.isDone) frame.run(body)
                        else frame.run(body).thenAccept { future.complete(it) }
                    } else if (!future.isDone) {
                        future.complete(event)
                    }
                }
            }
        }

        return future
    }

    private fun tryAcceptEvent(frame: ScriptFrame, condition: LiveData<Boolean>?, acceptEvent: () -> Unit) {
        if (condition == null) {
            acceptEvent()
            return
        }

        condition.get(frame, true).thenAccept { result ->
            if (!result) return@thenAccept
            acceptEvent()
        }
    }

    companion object {

        /**
         * event wait xxxEvent -priority xxx -filter/condition {...} {...}
         * */
        fun read(reader: QuestReader): ActionEventWait {
            val action = ActionEventWait(reader.readString())

            while (reader.nextPeek().startsWith('-')) {
                when (reader.nextToken().substring(1)) {
                    "unique", "id" -> action.taskId = reader.readString()
                    "priority", "p" -> action.priority = reader.readString()
                    "filter", "condition" -> action.condition = reader.readBoolean()
                    "async", "a" -> action.async = reader.readBoolean()
                    "timeout", "time", "t" -> action.timeout = reader.readInt()
                }
            }

            // 判断是否有执行体
            if (reader.hasNextToken("{")) {
                while (!reader.hasNextToken("}")) {
                    reader.mark()
                    when (reader.nextToken()) {
                        "-unique", "-id" -> action.taskId = reader.readString()
                        "-filter", "-condition" -> action.condition = reader.readBoolean()
                        "-priority", "-p" -> action.priority = reader.readString()
                        "-async", "-a" -> action.async = reader.readBoolean()
                        "-timeout", "-time", "-t" -> action.timeout = reader.readInt()
                        "on-timeout" -> action.onTimeout = reader.nextBlock()
                        else -> {
                            reader.reset()
                            action.actions += reader.nextBlock()
                        }
                    }
                }
            }

            return action
        }
    }
}