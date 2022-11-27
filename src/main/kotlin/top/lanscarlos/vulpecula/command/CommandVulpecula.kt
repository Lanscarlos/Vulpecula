package top.lanscarlos.vulpecula.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import top.lanscarlos.vulpecula.Context
import top.lanscarlos.vulpecula.utils.eval

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.command
 *
 * @author Lanscarlos
 * @since 2022-10-17 20:39
 */
@CommandHeader(name = "vulpecula", aliases = ["vul"])
object CommandVulpecula {

    @CommandBody
    val eval = subCommand {
        dynamic {
            execute<Player> { player, _, argument ->
                eval(argument, player, args = mapOf(
                    "player" to player,
                    "hand" to player.equipment?.itemInMainHand
                )).thenAccept {
                    player.sendMessage("§7[§f§lResult§7]§r $it")
                }
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            val info = Context.load()
            val player = sender as? Player ?: return@execute
            info.forEach { player.sendMessage(it) }
        }
    }

}