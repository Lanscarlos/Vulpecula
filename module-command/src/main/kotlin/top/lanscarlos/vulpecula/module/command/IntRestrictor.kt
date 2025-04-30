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
object IntRestrictor : Restrictor<Int> {

    override fun <T : ProxyCommandSender> restrict(sender: T, context: CommandContext<T>, argument: String): Boolean {
        return argument.toIntOrNull() != null
    }

    override fun convert(input: String): Int {
        return input.toIntOrNull() ?: error("Invalid input.")
    }

}