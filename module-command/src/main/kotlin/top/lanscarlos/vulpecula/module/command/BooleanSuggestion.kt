package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 14:03
 */
object BooleanSuggestion : Suggestion {

    override fun <T : ProxyCommandSender> suggest(sender: T, context: CommandContext<T>): List<String>? {
        return listOf("true", "false")
    }

}