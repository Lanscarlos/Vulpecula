package top.lanscarlos.vulpecula.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.common.platform.function.console
import top.lanscarlos.vulpecula.Vulpecula

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.command
 *
 * @author Lanscarlos
 * @since 2024-05-15 10:24
 */
object ReloadCommand {

    @CommandBody
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            val messages = Vulpecula.reload()
            if (sender is ProxyPlayer) {
                messages.forEach { sender.sendMessage(it) }
            }
            messages.forEach { console().sendMessage(it) }
        }

        dynamic("modules") {
            suggest {
                Vulpecula.reloadable.map { it.key }
            }
            execute<ProxyCommandSender> { sender, _, modules ->
                val messages = Vulpecula.reload(*modules.split(' ').toTypedArray())
                if (sender is ProxyPlayer) {
                    messages.forEach { sender.sendMessage(it) }
                }
                messages.forEach { console().sendMessage(it) }
            }
        }
    }

}