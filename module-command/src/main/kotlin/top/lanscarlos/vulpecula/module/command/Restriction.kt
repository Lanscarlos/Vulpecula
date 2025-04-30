package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 13:51
 */
interface Restriction {

    fun <T: ProxyCommandSender> restrict(sender: T, context: CommandContext<T>, argument: String): Boolean

}