package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 13:25
 */
interface Suggester<T> : Strategy<T> {

    fun <S: ProxyCommandSender> suggest(sender: S, context: CommandContext<S>): List<String>

}