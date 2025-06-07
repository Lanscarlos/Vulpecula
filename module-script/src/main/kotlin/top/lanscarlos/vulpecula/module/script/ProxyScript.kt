package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025-05-10 23:16
 */
class ProxyScript(override val id: String) : AbstractScript() {

    override val quest: Quest
        get() = error("The quest property is not available for ProxyScript.")

    override fun execute(
        sender: ProxyCommandSender?,
        args: List<Any?>,
        variables: Map<String, Any>
    ): ScriptTask {
        val script = ScriptService.get(id)
        return script.run(sender, args, variables)
    }

}