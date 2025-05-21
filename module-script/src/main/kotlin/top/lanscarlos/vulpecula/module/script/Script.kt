package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.script.selector.SelfSelector
import java.util.function.Consumer
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025-03-19 16:48
 */
interface Script {

    val id: String

    fun run(
        sender: ProxyCommandSender? = null,
        selector: SenderSelector = SelfSelector,
        args: List<Any?> = emptyList(),
        variables: Map<String, Any> = emptyMap(),
        onSuccess: Consumer<Any?> = Consumer {  },
        onFailure: Function<BacikalRuntimeException, Any?> = Function { it }
    ): ScriptTask

}