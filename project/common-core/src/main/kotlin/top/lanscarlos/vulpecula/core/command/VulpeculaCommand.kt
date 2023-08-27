package top.lanscarlos.vulpecula.core.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.info
import taboolib.module.kether.printKetherErrorMessage
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecula.bacikal.toBacikalQuest

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.command
 *
 * @author Lanscarlos
 * @since 2023-08-25 12:36
 */
@CommandHeader(
    name = "vulpecula",
    aliases = ["vul"],
    permission = "vulpecula.command.internal",
    permissionDefault = PermissionDefault.OP
)
object VulpeculaCommand {

    @CommandBody
    val eval = subCommand {
        dynamic {
            execute<ProxyCommandSender> { sender, _, content ->
                try {
                    val quest = content.toBacikalQuest("vulpecula-eval")
                    info("quest ${quest.name} is built successfully.")

                    quest.runActions {
                        this.sender = sender
                        if (sender is BukkitPlayer) {
                            setVariable("player", sender.player)
                            setVariable("hand", sender.player.equipment?.itemInMainHand)
                        }
                    }.thenAccept {
                        sender.sendMessage(" §5§l‹ ›§r §7Result: §f$it")
                    }
                } catch (e: Exception) {
                    e.printKetherErrorMessage()
                }
            }
        }
    }

    @CommandBody
    val reload = subCommand(VulpeculaReloadCommand.executor)

    @CommandBody
    val timing = subCommand(VulpeculaTimingCommand.executor)
}