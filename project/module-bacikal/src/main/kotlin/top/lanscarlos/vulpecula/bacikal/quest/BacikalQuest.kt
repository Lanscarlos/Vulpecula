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

    val source: KetherQuest

    fun runActions(context: BacikalQuestContext.() -> Unit): CompletableFuture<Any?>
}

typealias KetherQuest = Quest