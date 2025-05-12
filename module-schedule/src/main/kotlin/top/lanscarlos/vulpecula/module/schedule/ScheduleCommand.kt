package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * @author Lanscarlos
 * @since 2025/5/12 10:33
 */
object ScheduleCommand {

    @CommandBody
    val schedule = subCommand {
        literal("start", literal = start)
    }

    private val start: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScheduleService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                ScheduleService.get(id).activate()
                sender.sendMessage("schedule $id successfully started.")
            }
        }
    }

}