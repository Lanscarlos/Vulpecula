package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.common.platform.command.suggestPlayers
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import taboolib.module.chat.Components
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
                val builder = Components.text(MessageService.asInfo("module-schedule-command-task-list-header", id))
                for (task in schedule.tasks.values) {
                    builder.newLine()

                    val pid = task.pid

                    // 操作按钮
                    val pause = Components
                        .text(MessageService.asLang("module-schedule-task-operation-pause"))
                        .hoverText(MessageService.asLang("module-schedule-task-operation-pause-hover"))
                        .clickSuggestCommand("/vul schedule pause $id $pid")
                    val resume = Components
                        .text(MessageService.asLang("module-schedule-task-operation-resume"))
                        .hoverText(MessageService.asLang("module-schedule-task-operation-resume-hover"))
                        .clickSuggestCommand("/vul schedule resume $id $pid")
                    val terminate = Components
                        .text(MessageService.asLang("module-schedule-task-operation-terminate"))
                        .hoverText(MessageService.asLang("module-schedule-task-operation-terminate-hover"))
                        .clickSuggestCommand("/vul schedule stop $id $pid")
                    when (task.state) {
                        TaskState.WAITING,
                        TaskState.RUNNING -> {
                            builder.append(pause).append(" ")
                            builder.append(terminate).append(" ")
                        }
                        TaskState.PAUSED -> {
                            builder.append(resume).append(" ")
                            builder.append(terminate).append(" ")
                        }
                        TaskState.TERMINATED -> {}
                    }

                    // 消息体
                    val state = MessageService.asLang("module-schedule-task-state-${task.state.name.lowercase()}")
                    val message = MessageService.asLang("module-schedule-command-task-list-item", pid, state, task.counter)
                    builder.append(message)
                }

                // 发送消息
                if (sender is ProxyPlayer) {
                    builder.sendTo(sender)
                    console().sendMessage(builder.toLegacyText())
                } else {
                    sender.sendMessage(builder.toLegacyText())
                }
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