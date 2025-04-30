package top.lanscarlos.vulpecula.module.command

import org.bukkit.Bukkit
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 13:26
 */
object PlayerSuggestion : Suggestion {

    override fun <T : ProxyCommandSender> suggest(sender: T, context: CommandContext<T>): List<String>? {
        return Bukkit.getOnlinePlayers().map { it.name }
    }

}