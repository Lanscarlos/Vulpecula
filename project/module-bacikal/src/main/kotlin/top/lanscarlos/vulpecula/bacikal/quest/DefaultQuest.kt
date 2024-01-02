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
    override fun createContext(): BacikalQuestContext {
        return Bacikal.service.createQuestContext(this)
    }

    override fun runActions(func: BacikalQuestContext.() -> Unit): CompletableFuture<Any?> {
        return createContext().apply(func).runActions()
    }
}