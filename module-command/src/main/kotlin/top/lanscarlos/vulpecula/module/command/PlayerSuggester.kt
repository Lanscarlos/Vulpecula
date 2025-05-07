package top.lanscarlos.vulpecula.module.command

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 13:26
 */
object PlayerSuggester : Suggester {

    override fun suggest(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>): List<String> {
        return Bukkit.getOnlinePlayers().map { it.name }
    }

}