package top.lanscarlos.vulpecula.module.core.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.pluginVersion
import top.lanscarlos.vulpecula.common.core.command.CommandDevelop

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.core.command
 *
 * @author Lanscarlos
 * @since 2025/7/4 9:04
 */
@CommandDevelop
object DevelopInfoCommand {

    @CommandBody
    val version = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            sender.sendMessage("Vulpecula Version: $pluginVersion")
        }
    }

}