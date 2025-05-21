package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.script.selector.SelfSelector
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

    abstract fun run(
        sender: ProxyCommandSender? = null,
        args: List<Any?> = emptyList(),
        variables: Map<String, Any> = emptyMap(),
        onSuccess: Consumer<Any?> = Consumer {  },
        onFailure: Function<BacikalRuntimeException, Any?> = Function { it }
    ): ScriptTask

    override fun run(
        sender: ProxyCommandSender?,
        selector: SenderSelector,
        args: List<Any?>,
        variables: Map<String, Any>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): ScriptTask {
        val senders = selector.select(sender)
        if (senders.size == 1) {
            return run(sender, args, variables, onSuccess, onFailure)
        }
        when {
            senders.isEmpty() -> run(sender, args, variables, onSuccess, onFailure)
        }
    }

}