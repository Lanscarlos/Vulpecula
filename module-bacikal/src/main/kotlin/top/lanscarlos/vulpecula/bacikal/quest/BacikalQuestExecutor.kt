package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.library.kether.AbstractQuestContext
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.Quest
import taboolib.library.kether.QuestContext
import taboolib.module.kether.*
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2024-11-22 21:04
 */
object BacikalQuestExecutor {

    fun execute(quest: Quest, name: String, func: (ScriptContext) -> Unit): CompletableFuture<*> {
        return QuestExecutorContext(quest, name).also(func).runActions()
    }

    fun execute(quest: Quest, name: String, sender: ProxyCommandSender?, args: Map<String, Any?>): CompletableFuture<*> {
        return execute(quest, name) {
            it.sender = sender
            for (entry in args) {
                it[entry.key] = entry.value
            }
        }
    }

    class QuestExecutorContext(quest: Quest, val main: String) : ScriptContext(ScriptService, quest) {
        override fun createRootFrame(): QuestContext.Frame {
            return QuestExecutorFrame(this, main)
        }
    }

    /**
     * 海螺爹永远是你爹
     * @see taboolib.library.kether.AbstractQuestContext.SimpleNamedFrame
     * */
    class QuestExecutorFrame(context: ScriptContext, val name: String) : AbstractQuestContext.AbstractFrame(null, LinkedList(), AbstractQuestContext.SimpleVarTable(null), context) {

        var currentblock: Quest.Block? = null
        var nextBlock: Quest.Block? = null
        var stackPointer: Int = -1 // 当前动作的索引
        var nextPointer: Int = -1 // 下一个动作的索引

        init {
            context.quest.getBlock(name()).ifPresent(this::setNext)
        }

        override fun name(): String {
            return name // 入口函数名 main
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
            this.future = process(CompletableFuture.completedFuture(null)).exceptionally { ex ->
                ex.printKetherErrorMessage()
                val action = currentAction().orElse(null)
                val properties = action?.properties
                warning("Error in action: ${action?.action?.javaClass?.name}; properties: $properties")
                null
            }
            return this.future as CompletableFuture<T>
        }

        private fun process(source: CompletableFuture<*>): CompletableFuture<Any?> {
            var future = source
            while (future.isDone) {
                val next = nextAction() ?: return CompletableFuture.completedFuture(future.get())
                future = next.process(this)
            }
            return future.thenCompose { result ->
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