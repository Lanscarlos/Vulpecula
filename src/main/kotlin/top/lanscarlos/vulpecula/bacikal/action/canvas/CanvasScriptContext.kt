package top.lanscarlos.vulpecula.bacikal.action.canvas

import kotlinx.coroutines.*
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.kether.ExitStatus
import taboolib.library.kether.QuestContext
import taboolib.module.kether.*
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas
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
        if (isTerminated()) return
        forceTerminated = true
        val root = rootFrame() as? CanvasNamedFrame
        root?.cancel()
        super.terminate()
    }

    override fun runActions(): CompletableFuture<Any> {
        // 设置开始时间
        set(ActionCanvas.VARIABLE_DURATION_START, System.currentTimeMillis())
        return super.runActions()
    }

    /**
     * 是否已终止运行
     * */
    fun isTerminated(): Boolean {
        // 强制终止 或 任务已结束 或 跳出信号激活 或 退出状态不为空且（等待中 或 不在运行中）
        return forceTerminated || future?.isDone == true || breakLoop || exitStatus == ExitStatus.paused() || (exitStatus != null && (exitStatus.isWaiting || !exitStatus.isRunning))
    }

    fun start(variables: HashMap<String, Any?>? = null) {
        // 变量赋值
        val varTable = rootFrame().variables()
        varTable.clear()
        variables?.forEach { (key, value) -> varTable.set(key, value) }

        try {
            this@CanvasScriptContext.runActions().thenRun {
                // 运行完毕
                if (!forceTerminated) {
                    // 非强制结束，删除队列中的当前任务
                    quests.remove(super.id)
                }
            }
        } catch (e: Exception) {
            e.printKetherErrorMessage()
            quests.remove(super.id)
            e.printStackTrace()
        }
    }

    companion object {

        private val scope by lazy { CoroutineScope(Dispatchers.Default) }
        private val quests = ConcurrentHashMap<String, CanvasScriptContext>()

        fun submit(quest: CanvasQuest, extend: HashMap<String, Any?>? = null, force: Boolean = false) {
            if (force) {
                // 暂停当前任务
                quests[quest.id]?.terminate()

                // 创建新任务
                val context = CanvasScriptContext(quest)
                quests[quest.id] = context
                context.start(extend)

            } else if (quests[quest.id].let { it == null || it.isTerminated() }) {
                // 任务不存在或已终止，创建新任务
                quests[quest.id] = CanvasScriptContext(quest).also { it.start(extend) }
            }
        }

        @Awake(LifeCycle.DISABLE)
        fun onDisable() {
            // 关闭协程
            scope.cancel()
        }
    }

    class CanvasNamedFrame(
        parent: QuestContext.Frame?, name: String, questContext: QuestContext
    ) : SimpleNamedFrame(parent, LinkedList(), SimpleVarTable(parent), name, questContext) {

        private val context = questContext as? CanvasScriptContext ?: error("No CanvasScriptContext selected.")
        private val quest = questContext.quest as? CanvasQuest ?: error("No CanvasQuest selected.")
        private var task: Job? = null

        private val executor: suspend CoroutineScope.() -> Unit = {
            try {
                val delay = quest.period * 50L

                while (!context.isTerminated()) {
                    quest.body.process(this@CanvasNamedFrame).join()
                    delay(delay)
                }

                // 检测到跳出信号，结束
                this@CanvasNamedFrame.cancel()
            } catch (e: Exception) {
                this@CanvasNamedFrame.cancel()
                e.printKetherErrorMessage()
            }
        }

        /**
         * 取消任务
         * */
        fun cancel() {
            // 尾处理
            quest.postHandle.process(this).exceptionally {
                future.complete(null)
                it.printKetherErrorMessage()
                null
            }.thenRun {
                if (future?.isDone == true) return@thenRun
                future.complete(null)
            }

            task?.cancel()
            task = null
        }

        /*
        * 覆盖 SimpleNamedFrame 的 run() 方法
        * 实现自定义脚本运行
        * */
        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> run(): CompletableFuture<T> {
            varTable.initialize(this)
            future = CompletableFuture<Any?>()

            // 预处理
            quest.preHandle.process(this).exceptionally {
                future.complete(null)
                it.printKetherErrorMessage()
                null
            }.thenRun {
                // 启动协程执行绘画任务
                task = scope.launch(block = executor)
            }

            return future as CompletableFuture<T>
        }

    }
}