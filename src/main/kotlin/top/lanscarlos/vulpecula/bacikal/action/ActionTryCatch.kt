package top.lanscarlos.vulpecula.bacikal.action

import taboolib.common.platform.function.console
import taboolib.module.kether.run
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.bacikal
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action
 *
 * @author Lanscarlos
 * @since 2023-03-25 23:40
 */
object ActionTryCatch {

    @BacikalParser(
        id = "try-catch",
        aliases = ["try"]
    )
    fun parser() = bacikal {
        combineOf(
            action(),
            optional("catch", then = optional("with", then = multiline(display = "catch types"), def = emptyList()).union(action()))
        ) { tryAction, addition ->
            try {
                this.run(tryAction)
            } catch (e: Exception) {
                e.printStackTrace()
                val exceptionName = e::class.java.simpleName
                console().sendLang("Action-TryCatch-Warning", exceptionName, e.localizedMessage)

                if (addition?.first?.any { it.equals(exceptionName, true) } == true) {
                    val newFrame = this.newFrame(addition.second)
                    newFrame.variables().set("error", exceptionName)
                    newFrame.variables().set("exception", exceptionName)
                    newFrame.variables().set("exceptionInfo", e.localizedMessage)
                    newFrame.variables().set("@Exception", e)
                    newFrame.run<Any?>()
                } else {
                    CompletableFuture.completedFuture(null)
                }
            }
        }
    }

}