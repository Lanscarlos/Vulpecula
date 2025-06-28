package top.lanscarlos.vulpecula.module.bacikal.quest

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.*
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.module.bacikal.exception.QuestRuntimeException
import top.lanscarlos.vulpecula.module.bacikal.exception.QuestTimeoutException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2024-11-22 21:04
 */
object BacikalQuestExecutor {

    fun execute(quest: Quest, main: String, timeout: Long, sender: ProxyCommandSender?, args: Map<String, Any?>): CompletableFuture<Any?> {
        return executeLater(quest, main, timeout, sender, args).runActions()
    }

    fun execute(quest: Quest, main: String, timeout: Long, func: (ScriptContext) -> Unit): CompletableFuture<Any?> {
        return executeLater(quest, main, timeout, func).runActions()
    }

    fun executeLater(quest: Quest, main: String, timeout: Long, sender: ProxyCommandSender?, args: Map<String, Any?>): ScriptContext {
        return executeLater(quest, main, timeout) {
            it.sender = sender
            for (entry in args) {
                it[entry.key] = entry.value
            }
        }
    }

    fun executeLater(quest: Quest, main: String, timeout: Long, func: (ScriptContext) -> Unit): ScriptContext {
        val context = object : ScriptContext(ScriptService, quest) {
            override fun createRootFrame(): QuestContext.Frame {
                return QuestExecutorFrame(this, main, timeout)
            }
        }
        return context.also(func)
    }

    /**
     * 海螺爹永远是你爹
     * @see taboolib.library.kether.AbstractQuestContext.SimpleNamedFrame
     * */
    class QuestExecutorFrame(
        context: ScriptContext,
        private val main: String,
        val timeout: Long
    ) :
        AbstractQuestContext.AbstractFrame(null, LinkedList(), AbstractQuestContext.SimpleVarTable(null), context) {

        var currentblock: Quest.Block? = null
        var nextBlock: Quest.Block? = null
        var stackPointer: Int = -1 // 当前动作的索引
        var nextPointer: Int = -1 // 下一个动作的索引

        init {
            context.quest.getBlock(name()).ifPresent(this::setNext)
        }

        override fun name(): String {
            return main // 入口函数名 main
        }

        override fun setNext(action: ParsedAction<*>) {
            if (currentblock != null) {
                nextPointer = currentblock!!.indexOf(action)
                if (nextPointer == -1) {
                    // 当前代码块中没有找到该动作
                    nextBlock = null
                }
            }

            if (nextBlock == null) {
                // 语句在其他代码块中
                val block = context().quest.blockOf(action).orElseThrow {
                    IllegalArgumentException("$action is not in quest")
                }
                nextBlock = block
                nextPointer = block.indexOf(action)
            }
        }

        override fun setNext(block: Quest.Block) {
            this.nextBlock = block
            this.nextPointer = 0
        }

        override fun currentAction(): Optional<ParsedAction<*>> {
            if (currentblock == null || stackPointer == -1) {
                return Optional.empty()
            }
            return currentblock?.get(stackPointer) ?: Optional.empty()
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> run(): CompletableFuture<T> {
            if (future != null) {
                error("Already running.")
            }
            this.varTable.initialize(this)
            this.future = process(CompletableFuture.completedFuture(null))
            if (timeout > 0) {
                this.future = this.future.orTimeout(timeout, TimeUnit.MILLISECONDS)
            }
            this.future = this.future.exceptionally { ex ->
                // 发生异常, 终止程序
                this.context().setExitStatus(ExitStatus.paused())
                when (ex) {
                    is TimeoutException -> {
                        val action = currentAction().get()
                        val properties = action.properties
                        throw QuestTimeoutException(ex, context().quest, properties, timeout)
                    }
                    is CompletionException -> {
                        val action = currentAction().get()
                        val properties = action.properties
                        throw QuestRuntimeException(ex.cause!!, context().quest, properties)
                    }
                    else -> {
                        error("Unexpected exception: ${ex.javaClass.name}")
                    }
                }
            }
            return this.future as CompletableFuture<T>
        }

        private fun process(source: CompletableFuture<*>): CompletableFuture<Any?> {
            return source.thenCompose { result ->
                val action = nextAction() ?: return@thenCompose CompletableFuture.completedFuture(result)
                val task = action.process(this)
                process(task)
            }
        }

        private fun nextAction(): ParsedAction<*>? {
            // 检查终止状态
            if (this.context().exitStatus.isPresent) {
                // 程序终止
                return null
            }

            // 清理
            this.cleanup()
            this.frames.removeIf(QuestContext.Frame::isDone)

            // 检索下一条语句
            if (nextBlock != null && nextPointer != -1) {
                currentblock = nextBlock
                stackPointer = nextPointer
            }
            return nextBlock?.get(nextPointer++)?.orElse(null)
        }

        private fun cleanup() {
            while (closeables.isNotEmpty()) {
                try {
                    (closeables.pollFirst() as AutoCloseable).close()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

    }

}