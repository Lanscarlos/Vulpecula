package top.lanscarlos.vulpecula.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import top.lanscarlos.vulpecula.VulpeculaContext
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
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val eval = subCommand {
        dynamic {
            execute<CommandSender> { sender, _, argument ->
                val args = if (sender is Player) {
                    mapOf(
                        "player" to sender,
                        "hand" to sender.equipment?.itemInMainHand
                    )
                } else null

                eval(argument, sender, args = args).thenAccept {
                    sender.sendMessage("§7[§f§lResult§7]§r $it")
                }
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            val info = VulpeculaContext.load()
            val player = sender as? Player ?: return@execute
            info.forEach { player.sendMessage(it) }
        }
    }

    @CommandBody
    val script = subCommand(CommandScript.main)

}