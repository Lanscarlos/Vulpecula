package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.common.platform.command.suggestPlayers
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import top.lanscarlos.vulpecula.common.message.MessageService
import top.lanscarlos.vulpecula.common.message.info
import top.lanscarlos.vulpecula.common.message.infoLiteral

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
        literal("stop", literal = stop)
        literal("detail", literal = detail)
        literal("reload", literal = reload)
    }

    private val start: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScheduleService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                val task = ScheduleService.get(id).start()
                sender.info("module-schedule-command-run-success", id, task.pid, "null", "[]")
            }
        }.dynamic("pid") {
            execute<ProxyCommandSender> { sender, context, pid ->
                val id = context["id"]
                ScheduleService.get(id).start(
                    pid = pid
                )
                sender.info("module-schedule-command-run-success", id, pid, "null", "[]")
            }
        }.dynamic("sender") {
            suggestPlayers(listOf("@NULL", "@SELF", "@CONSOLE"))
            execute<ProxyCommandSender> { sender, context, value ->
                val id = context["id"]
                val pid = context["pid"]
                val runtimeSender = value.toSender(sender)
                ScheduleService.get(id).start(
                    pid = pid,
                    sender = runtimeSender
                )
                sender.info("module-schedule-command-run-success", id, pid, runtimeSender?.name ?: "null", "[]")
            }
        }.dynamic("args") {
            execute<ProxyCommandSender> { sender, context, value ->
                val id = context["id"]
                val pid = context["pid"]
                val runtimeSender = context["sender"].toSender(sender)
                val args = value.split(' ')
                ScheduleService.get(id).start(
                    pid = pid,
                    sender = runtimeSender,
                    args = args
                )
                sender.info("module-schedule-command-run-success", id, pid, runtimeSender?.name ?: "null", args)
            }
        }
    }

    private val stop: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScheduleService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                ScheduleService.get(id).stop("*")
                sender.info("module-schedule-command-stop-all", id)
            }
        }.dynamic("pid") {
            suggest { ScheduleService.get(ctx["id"]).tasks.keys.toList() }
            execute<ProxyCommandSender> { sender, context, pid ->
                val id = context["id"]
                ScheduleService.get(id).stop(pid)
                sender.info("module-schedule-command-stop-task", id, pid)
            }
        }
    }

    private val detail: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScheduleService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                val schedule = ScheduleService.get(id)
                val builder = StringBuilder(MessageService.asLang("module-schedule-command-task-list-header", id))
                for (task in schedule.tasks.values) {
                    val pid = task.pid
                    val state = MessageService.asLang("module-schedule-task-state-${task.state.name.lowercase()}")
                    val message = MessageService.asLang("module-schedule-command-task-list-item", pid, state, task.counter)
                    builder.append('\n').append(message)
                }
                sender.infoLiteral(builder.toString())
            }
        }
    }

    private val reload: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            ScheduleService.reload(sender)
        }
    }

    private fun String.toSender(sender: ProxyCommandSender): ProxyCommandSender? {
        return when {
            this.equals("@NULL", true) -> null
            this.equals("@SELF", true) -> sender
            this.equals("@CONSOLE", true) -> console()
            else -> onlinePlayers().find { it.name.equals(this, true) }
                ?: error("No sender found for $this.")
        }
    }

}