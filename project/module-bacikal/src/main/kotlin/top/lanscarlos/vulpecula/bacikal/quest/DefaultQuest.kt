package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.library.kether.Quest
import top.lanscarlos.vulpecula.bacikal.Bacikal
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 22:32
 */
open class DefaultQuest(override val name: String, override val content: String, override val source: Quest) : BacikalQuest {

    override var executor: BacikalQuestExecutor = Bacikal.service.questExecutor

    override fun createContext(entry: String): BacikalQuestContext {
        return executor.createContext(this, entry)
    }

    override fun runActions(entry: String, func: BacikalQuestContext.() -> Unit): CompletableFuture<Any?> {
        return createContext(entry).apply(func).runActions()
    }
}