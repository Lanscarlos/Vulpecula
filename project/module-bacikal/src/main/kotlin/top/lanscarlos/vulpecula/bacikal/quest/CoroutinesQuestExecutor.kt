package top.lanscarlos.vulpecula.bacikal.quest

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import taboolib.library.kether.AbstractQuestContext.AbstractFrame
import taboolib.library.kether.AbstractQuestContext.SimpleVarTable
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.Quest
import taboolib.library.kether.QuestContext
import taboolib.module.kether.ScriptContext
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-25 11:39
 */
object CoroutinesQuestExecutor : BacikalQuestExecutor {

    private val scope by lazy { CoroutineScope(Dispatchers.Default) }

    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        scope.cancel()
    }

    override fun createContext(quest: BacikalQuest, entry: String): BacikalQuestContext {
        return CoroutinesQuestContext(quest, entry)
    }

    override fun execute(quest: BacikalQuest, entry: String, func: BacikalQuestContext.() -> Unit): CompletableFuture<Any?> {
        return createContext(quest, entry).also(func).runActions()
    }

    class CoroutinesQuestContext(quest: BacikalQuest, entry: String) : AbstractQuestExecutor(quest, entry) {
        override fun createRootFrame(context: InnerContext): QuestContext.Frame {
            return InnerRootFrame(context)
        }

        inner class InnerRootFrame(context: ScriptContext) :
            AbstractFrame(null, LinkedList(), SimpleVarTable(null), context) {

            var block: Quest.Block? = null
            var current: Int = -1

            init {
                quest.source.getBlock(QuestContext.BASE_BLOCK).ifPresent(this::setNext)
            }

            override fun name(): String {
                return QuestContext.BASE_BLOCK
            }

            override fun currentAction(): Optional<ParsedAction<*>> {
                return block?.get(current) ?: Optional.empty()
            }

            override fun setNext(action: ParsedAction<*>) {
                current = block?.indexOf(action) ?: -1

                if (current == -1) {
                    block = quest.source.blockOf(action).orElse(null)
                    current = block?.indexOf(action) ?: -1
                }
            }

            override fun setNext(block: Quest.Block) {
                this.block = block
                this.current = 0
            }

            fun nextAction(): ParsedAction<*>? {
                return block?.get(current++)?.orElse(null)
            }

            @Suppress("UNCHECKED_CAST")
            override fun <T : Any?> run(): CompletableFuture<T> {
                val future = CompletableFuture<T>()
                this.future = future

                scope.launch {
                    info("running Coroutines...")
                    try {
                        var cache: Any? = null
                        while (!context.exitStatus.isPresent) {
                            cleanup()
                            frames.removeIf(QuestContext.Frame::isDone)

                            val action = nextAction() ?: break
                            cache = action.process(this@InnerRootFrame).join()
                            info("running cache: $cache")
                        }
                        info("final cache: $cache")
                        future.complete(cache as? T)
                    } catch (e: Throwable) {
                        future.completeExceptionally(e)
                    }
                }

                return future
            }

            fun cleanup() {
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
}