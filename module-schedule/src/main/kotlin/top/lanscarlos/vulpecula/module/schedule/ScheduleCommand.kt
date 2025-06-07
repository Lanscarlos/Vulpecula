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
import top.lanscarlos.vulpecula.common.lang.asLang
import top.lanscarlos.vulpecula.common.lang.error
import top.lanscarlos.vulpecula.common.lang.info

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
        literal("pause", literal = pause)
        literal("resume", literal = resume)
        literal("stop", literal = stop)
        literal("detail", literal = detail)
        literal("reload", literal = reload)
    }

    private val start: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScheduleService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                try {
                    val task = ScheduleService.get(id).start()
                    sender.info { asLang("module-schedule-command-run-success", id, task.pid, "null", "[]") }
                } catch (e: Exception) {
                    sender.error { asLang("module-schedule-command-run-failure", id, e.localizedMessage) }
                }
            }
        }.dynamic("pid") {
            execute<ProxyCommandSender> { sender, context, pid ->
                val id = context["id"]
                try {
                    ScheduleService.get(id).start(
                        pid = pid
                    )
                    sender.info { asLang("module-schedule-command-run-success", id, pid, "null", "[]") }
                } catch (e: Exception) {
                    sender.error { asLang("module-schedule-command-run-failure", id, e.localizedMessage) }
                }
            }
        }.dynamic("sender") {
            suggestPlayers(listOf("@NULL", "@SELF", "@CONSOLE"))
            execute<ProxyCommandSender> { sender, context, value ->
                val id = context["id"]
                try {
                    val pid = context["pid"]
                    val runtimeSender = value.toSender(sender)
                    ScheduleService.get(id).start(
                        pid = pid,
                        sender = runtimeSender
                    )
                    sender.info { asLang("module-schedule-command-start-success", id, pid, runtimeSender?.name ?: "null", "[]") }
                } catch (e: Exception) {
                    sender.error { asLang("module-schedule-command-run-failure", id, e.localizedMessage) }
                }
            }
        }.dynamic("args") {
            execute<ProxyCommandSender> { sender, context, value ->
                val id = context["id"]
                try {
                    val pid = context["pid"]
                    val runtimeSender = context["sender"].toSender(sender)
                    val args = value.split(' ')
                    ScheduleService.get(id).start(
                        pid = pid,
                        sender = runtimeSender,
                        args = args
                    )
                    sender.info { asLang("module-schedule-command-start-success", id, pid, runtimeSender?.name ?: "null", args) }
                } catch (e: Exception) {
                    sender.error { asLang("module-schedule-command-run-failure", id, e.localizedMessage) }
                }
            }
        }
    }

    private val pause: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScheduleService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                try {
                    ScheduleService.get(id).pause("*")
                    sender.info { asLang("module-schedule-command-pause-all-success", id) }
                } catch (e: Exception) {
                    sender.error { asLang("module-schedule-command-pause-all-failure", id) }
                }
            }
        }.dynamic("pid") {
            suggest { ScheduleService.get(ctx["id"]).tasks.keys.toList() }
            execute<ProxyCommandSender> { sender, context, pid ->
                val id = context["id"]
                try {
                    ScheduleService.get(id).pause(pid)
                    sender.info { asLang("module-schedule-command-pause-task-success", id, pid) }
                } catch (e: Exception) {
                    sender.error { asLang("module-schedule-command-pause-task-failure", id, pid, e.localizedMessage) }
                }
            }
        }
    }

    private val resume: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScheduleService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                try {
                    ScheduleService.get(id).resume("*")
                    sender.info { asLang("module-schedule-command-resume-all-success", id) }
                } catch (e: Exception) {
                    sender.error { asLang("module-schedule-command-resume-all-failure", id, e.localizedMessage) }
                }
            }
        }.dynamic("pid") {
            suggest { ScheduleService.get(ctx["id"]).tasks.keys.toList() }
            execute<ProxyCommandSender> { sender, context, pid ->
                val id = context["id"]
                try {
                    ScheduleService.get(id).resume(pid)
                    sender.info { asLang("module-schedule-command-resume-task-success", id, pid) }
                } catch (e: Exception) {
                    sender.error { asLang("module-schedule-command-resume-task-failure", id, pid, e.localizedMessage) }
                }
            }
        }
    }

    private val stop: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScheduleService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                try {
                    ScheduleService.get(id).stop("*")
                    sender.info { asLang("module-schedule-command-stop-all-success", id) }
                } catch (e: Exception) {
                    sender.error { asLang("module-schedule-command-stop-all-failure", id, e.localizedMessage) }
                }
            }
        }.dynamic("pid") {
            suggest { ScheduleService.getOrNull(ctx["id"])?.tasks?.keys?.toList() }
            execute<ProxyCommandSender> { sender, context, pid ->
                val id = context["id"]
                try {
                    ScheduleService.get(id).stop(pid)
                    sender.info { asLang("module-schedule-command-stop-task-success", id, pid) }
                } catch (e: Exception) {
                    sender.error { asLang("module-schedule-command-stop-task-failure", id, pid, e.localizedMessage) }
                }
            }
        }
    }

    private val detail: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            showScheduleDetail(sender, "*")
        }

        dynamic("id") {
            suggest { ScheduleService.keys().toList().plus("*") }
            execute<ProxyCommandSender> { sender, _, id ->
                showScheduleDetail(sender, id)
            }
        }
    }

    private val reload: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            ScheduleService.reload(sender)
        }
    }

    private fun showScheduleDetail(sender: ProxyCommandSender, id: String) {
        // 获取任务列表
        val tasks = if (id == "*") {
            ScheduleService.values().flatMap { it.tasks.values }
        } else {
            ScheduleService.get(id).tasks.values
        }

        val builder = Components.text(asLang("module-schedule-command-detail-header"))
        for (task in tasks) {
            builder.newLine()

            // 消息项
            val pid = task.pid
            val state = asLang("module-schedule-task-state-${task.state.name.lowercase()}")
            val message = asLang("module-schedule-command-detail-item", task.id, pid, state, task.counter)
            builder.append(message)

            if (sender !is ProxyPlayer) {
                // 非玩家操作者不显示操作按钮
                continue
            }

            // 操作按钮
            val pause = Components
                .text(asLang("module-schedule-task-operation-pause"))
                .hoverText(asLang("module-schedule-task-operation-pause-hover"))
                .clickSuggestCommand("/vul schedule pause ${task.id} $pid")
            val resume = Components
                .text(asLang("module-schedule-task-operation-resume"))
                .hoverText(asLang("module-schedule-task-operation-resume-hover"))
                .clickSuggestCommand("/vul schedule resume ${task.id} $pid")
            val terminate = Components
                .text(asLang("module-schedule-task-operation-terminate"))
                .hoverText(asLang("module-schedule-task-operation-terminate-hover"))
                .clickSuggestCommand("/vul schedule stop ${task.id} $pid")
            when (task.state) {
                TaskState.WAITING,
                TaskState.RUNNING -> {
                    builder.append(" ").append(pause)
                    builder.append(" ").append(terminate)
                }
                TaskState.PAUSED -> {
                    builder.append(" ").append(resume)
                    builder.append(" ").append(terminate)
                }
                TaskState.TERMINATED -> {}
            }
        }

        // 尾部
        val footer = asLang("module-schedule-command-detail-footer")
        if (footer.isNotEmpty()) {
            builder.append(footer)
        }

        // 发送消息
        if (sender is ProxyPlayer) {
            builder.sendTo(sender)
        } else {
            sender.info { builder.toLegacyText() }
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