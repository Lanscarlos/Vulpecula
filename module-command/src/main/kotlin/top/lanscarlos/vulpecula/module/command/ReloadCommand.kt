package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import top.lanscarlos.vulpecula.common.utils.withConsole

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
                CommandService.reload(sender.withConsole())
            }
        }
    }

}