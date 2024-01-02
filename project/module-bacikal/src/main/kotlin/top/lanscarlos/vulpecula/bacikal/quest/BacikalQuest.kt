package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.library.kether.Quest
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-20 22:02
 */
interface BacikalQuest {

    val name: String

    /**
     * 源码
     * */
    val content: String

    val source: KetherQuest

    fun createContext(): BacikalQuestContext

    fun runActions(func: BacikalQuestContext.() -> Unit): CompletableFuture<Any?>
}

typealias KetherQuest = Quest