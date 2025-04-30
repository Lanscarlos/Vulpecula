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

    fun <T: ProxyCommandSender> execute(sender: T, context: CommandContext<T>, argument: String)

}