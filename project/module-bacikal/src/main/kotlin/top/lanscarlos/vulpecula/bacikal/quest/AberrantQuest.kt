package top.lanscarlos.vulpecula.bacikal.quest

import java.lang.Exception
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-25 01:44
 */
class AberrantQuest(override val name: String, override val content: String, val exception: Exception) : BacikalQuest {

    override val source: KetherQuest
        get() = error("Quest $name is aberrant with compiling. exception: ${exception.localizedMessage}")

    override fun createContext(): BacikalQuestContext {
        error("Quest $name is aberrant with compiling. exception: ${exception.localizedMessage}")
    }

    override fun runActions(func: BacikalQuestContext.() -> Unit): CompletableFuture<Any?> {
        error("Quest $name is aberrant with compiling. exception: ${exception.localizedMessage}")
    }

}