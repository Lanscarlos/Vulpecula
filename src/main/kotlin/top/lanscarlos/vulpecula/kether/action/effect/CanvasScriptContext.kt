package top.lanscarlos.vulpecula.kether.action.effect

import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.library.kether.QuestContext
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.utils.toBoolean
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.effect
 *
 * @author Lanscarlos
 * @since 2022-11-08 13:43
 */
class CanvasScriptContext(quest: CanvasQuest) : ScriptContext(ScriptService, quest) {

    var forceTerminated = false

    init {
        super.id = quest.id
    }

    override fun createRootFrame(): QuestContext.Frame {
        return CanvasNamedFrame(null, BASE_BLOCK, this)
    }

    override fun terminate() {
        val root = rootFrame() as? CanvasNamedFrame
        root?.task?.cancel()
        forceTerminated = true
        super.terminate()
    }

    override fun runActions(): CompletableFuture<Any> {
        set(ActionCanvas.VARIABLE_DURATION_START, System.currentTimeMillis())
        return super.runActions()
    }

    fun start(variables: HashMap<String, Any?>? = null) {

        if (future != null) {
            if (exitStatus != null && !exitStatus.isRunning) {
                // 已终止
                submit(quest as CanvasQuest, rootFrame().deepVars(), true)
                return
            } else {
                // 仍在运行
                return
            }
        }

        rootFrame().variables().clear()
        variables?.forEach { (key, value) ->
            set(key, value)
        }

        try {
            this@CanvasScriptContext.runActions().thenRun {
                // 运行完毕
                if (!forceTerminated) {
                    // 非强制结束，删除队列中的当前任务
                    quests.remove(super.id)
                }
            }
        } catch (e: Exception) {
            quests.remove(super.id)
            e.printStackTrace()
        }
    }

    companion object {

        private val quests = ConcurrentHashMap<String, CanvasScriptContext>()

        fun submit(quest: CanvasQuest, extend: HashMap<String, Any?>? = null, force: Boolean = false) {
            if (force) {
                quests[quest.id]?.terminate()
                val context = CanvasScriptContext(quest)
                quests[quest.id] = context
                context.start(extend)
            } else {
                quests.computeIfAbsent(quest.id) {
                    CanvasScriptContext(quest)
                }.start(extend)
            }
        }
    }

    class CanvasNamedFrame(
        parent: QuestContext.Frame?, name: String, questContext: QuestContext
    ) : SimpleNamedFrame(parent, LinkedList(), SimpleVarTable(parent), name, questContext) {

        var task: PlatformExecutor.PlatformTask? = null

        /*
        * 覆盖 SimpleNamedFrame 的 run() 方法
        * 实现自定义脚本运行
        * */
        @SuppressWarnings("unchecked")
        override fun <T : Any?> run(): CompletableFuture<T> {
            varTable.initialize(this)
            future = CompletableFuture<Any?>()
            val quest = questContext.quest as? CanvasQuest ?: error("No CanvasQuest selected.")

            // 预处理
            quest.preHandle.process(this).thenRun {

                // 异步循环执行绘画任务
                task = submit(async = true, period = quest.period.toLong()) {
                    try {
                        // 判断运行条件, 用 join() 阻塞
                        if (!quest.condition.process(this@CanvasNamedFrame).join().toBoolean(false)) {
                            // 条件不满足，结束
                            cancel()

                            // 尾处理
                            quest.postHandle.process(this@CanvasNamedFrame).thenRun {
                                future.complete(null)
                            }
                            return@submit
                        }

                        quest.body.process(this@CanvasNamedFrame).join()
                    } catch (e: Exception) {
                        cancel()
                        e.printStackTrace()
                    }
                }
            }
            return future as CompletableFuture<T>
        }

    }
}