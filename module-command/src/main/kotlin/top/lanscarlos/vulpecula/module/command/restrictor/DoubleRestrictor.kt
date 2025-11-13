package top.lanscarlos.vulpecula.module.command.restrictor

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import top.lanscarlos.vulpecula.module.command.Restrictor

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 14:26
 */
object DoubleRestrictor : Restrictor {

    override fun restrict(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String): Boolean {
        return argument.toDoubleOrNull() != null
    }

}