package top.lanscarlos.vulpecula.core.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.function.console
import top.lanscarlos.vulpecula.core.VulpeculaContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.command
 *
 * @author Lanscarlos
 * @since 2023-08-27 12:26
 */
object VulpeculaReloadCommand {

    val executor: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            val messages = VulpeculaContext.reload()
            if (sender is ProxyPlayer) {
                messages.forEach { sender.sendMessage(it) }
            }
            messages.forEach { console().sendMessage(it) }
        }

        dynamic("modules") {
            execute<ProxyCommandSender> { sender, _, modules ->
                val messages = VulpeculaContext.reload(*modules.split(' ').toTypedArray())
                if (sender is ProxyPlayer) {
                    messages.forEach { sender.sendMessage(it) }
                }
                messages.forEach { console().sendMessage(it) }
            }
        }
    }
}