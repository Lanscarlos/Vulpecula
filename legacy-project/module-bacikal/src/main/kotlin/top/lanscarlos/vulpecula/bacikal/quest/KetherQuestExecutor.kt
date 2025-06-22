package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.library.kether.AbstractQuestContext.SimpleNamedFrame
import taboolib.library.kether.AbstractQuestContext.SimpleVarTable
import taboolib.library.kether.QuestContext
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-27 15:54
 */
object KetherQuestExecutor : BacikalQuestExecutor {

    override fun createContext(quest: BacikalQuest, entry: String): BacikalQuestContext {
        return KetherQuestContext(quest, entry)
    }

    override fun execute(quest: BacikalQuest, entry: String, func: BacikalQuestContext.() -> Unit): CompletableFuture<Any?> {
        return createContext(quest, entry).also(func).runActions()
    }

    class KetherQuestContext(quest: BacikalQuest, entry: String) : AbstractQuestExecutor(quest, entry) {
        override fun createRootFrame(context: InnerContext): QuestContext.Frame {
            return SimpleNamedFrame(null, mutableListOf(), SimpleVarTable(null), entry, context)
        }
    }
}