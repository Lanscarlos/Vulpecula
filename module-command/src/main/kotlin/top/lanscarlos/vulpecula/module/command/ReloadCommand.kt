package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/5/6 11:23
 */
object ReloadCommand {

    @CommandBody
    val command = subCommand {
        literal("reload") {
            execute<ProxyCommandSender> { sender, _, _ ->
                CommandService.reload(sender)
            }
        }
    }

}