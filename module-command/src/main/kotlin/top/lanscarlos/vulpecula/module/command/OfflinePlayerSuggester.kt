package top.lanscarlos.vulpecula.module.command

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 14:18
 */
object OfflinePlayerSuggester : Suggester {

    override fun suggest(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>): List<String> {
        return Bukkit.getOfflinePlayers().mapNotNull { it.name }
    }

}