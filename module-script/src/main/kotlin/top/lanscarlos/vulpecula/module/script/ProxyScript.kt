package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import java.util.function.Consumer
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025-05-10 23:16
 */
class ProxyScript(override val id: String) : Script {

    override fun run(
        sender: ProxyCommandSender?,
        selector: SenderSelector,
        args: List<Any?>,
        variables: Map<String, Any>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): ScriptTask {
        val script = ScriptService.get(id)
        return script.run(sender, selector, args, variables, onSuccess, onFailure)
    }

}