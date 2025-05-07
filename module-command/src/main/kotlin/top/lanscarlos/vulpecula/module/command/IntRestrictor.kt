package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 14:22
 */
object IntRestrictor : Restrictor {

    override fun restrict(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String): Boolean {
        return argument.toIntOrNull() != null
    }

}