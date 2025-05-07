package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/30 9:30
 */
interface Executor {

    fun execute(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String)

}