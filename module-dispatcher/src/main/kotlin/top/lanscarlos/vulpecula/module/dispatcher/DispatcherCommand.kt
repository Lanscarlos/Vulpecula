package top.lanscarlos.vulpecula.module.dispatcher

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.common.utils.withConsole

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/9
 */
object DispatcherCommand {

    @CommandBody
    val dispatcher = subCommand {
        literal("reload", literal = reload)
        literal("enable", literal = enable)
        literal("disable", literal = disable)
    }

    private val enable: CommandComponent.() -> Unit = {
        dynamic("id") {
            execute<ProxyCommandSender> { sender, _, id ->
                try {
                    DispatcherService.get(id).enable()
                    sender.info { asLang("module-dispatcher-command-enable-success", id) }
                } catch (e: Exception) {
                    sender.error { asLang("module-dispatcher-command-enable-failure", id, e.localizedMessage) }
                }
            }
        }
    }

    private val disable: CommandComponent.() -> Unit = {
        dynamic("id") {
            execute<ProxyCommandSender> { sender, _, id ->
                try {
                    DispatcherService.get(id).disable()
                    sender.info { asLang("module-dispatcher-command-disable-success", id) }
                } catch (e: Exception) {
                    sender.error { asLang("module-dispatcher-command-disable-failure", id, e.localizedMessage) }
                }
            }
        }
    }

    private val reload: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            DispatcherService.reload(sender.withConsole())
        }
    }

}