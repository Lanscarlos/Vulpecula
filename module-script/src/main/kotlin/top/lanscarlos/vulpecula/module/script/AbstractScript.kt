package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025/5/20 13:55
 */
abstract class AbstractScript : Script {

    abstract val quest: Quest

    fun handle(result: Any?, ex: Throwable): Any? {

        ScriptService.clearTask(pid)
        if (e == null) {
            onSuccess.accept(result)
            return@handle result
        }
        val ex = e.cause as BacikalRuntimeException
        ex.printKetherMessage()
        return@handle onFailure.apply(ex)
    }

}