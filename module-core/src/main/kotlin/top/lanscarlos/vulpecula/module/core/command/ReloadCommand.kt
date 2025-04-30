package top.lanscarlos.vulpecula.module.core.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import top.lanscarlos.vulpecula.common.config.Configs

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.core.command
 *
 * @author Lanscarlos
 * @since 2025/4/30 14:14
 */
object ReloadCommand {

    @CommandBody
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            val logs = Configs.reload()
            for (log in logs) {
                sender.sendMessage(log)
            }
        }

        dynamic("service") {
            suggest { Configs.services.map { it.id } }
            execute<ProxyCommandSender> { sender, _, serviceId ->
                val log = Configs.services.first { it.id == serviceId }.load()
                sender.sendMessage(log)
            }
        }
    }

}