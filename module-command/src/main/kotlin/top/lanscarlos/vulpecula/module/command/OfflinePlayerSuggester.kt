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
object OfflinePlayerSuggester : Suggester<OfflinePlayer> {

    override fun <T : ProxyCommandSender> suggest(sender: T, context: CommandContext<T>): List<String> {
        return Bukkit.getOfflinePlayers().mapNotNull { it.name }
    }

    override fun convert(input: String): OfflinePlayer {
        return Bukkit.getOfflinePlayers().find { it.name == input } ?: error("Invalid input.")
    }

}