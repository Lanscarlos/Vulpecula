package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/5/6 11:47
 */
class ListSuggester(list: List<*>) : Suggester {

    val list = list.map { it.toString() }

    override fun suggest(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>): List<String> {
        return list
    }

}