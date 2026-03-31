package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import taboolib.module.chat.Components
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.common.utils.withConsole

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
                    Lang.MODULE_SCHEDULE_COMMAND_START_SUCCESS.info(sender, id, task.pid, "[]")
                } catch (e: Exception) {
                    Lang.MODULE_SCHEDULE_COMMAND_START_FAILURE.error(sender, id, e.localizedMessage)
                }
            }
        }.dynamic("pid") {
            execute<ProxyCommandSender> { sender, context, pid ->
                val id = context["id"]
                try {
                    ScheduleService.get(id).start(
                        pid = pid
                    )
                    Lang.MODULE_SCHEDULE_COMMAND_START_SUCCESS.info(sender, id, pid, "[]")
                } catch (e: Exception) {
                    Lang.MODULE_SCHEDULE_COMMAND_START_FAILURE.error(sender, id, e.localizedMessage)
                }
            }
        }.dynamic("args") {
            execute<ProxyCommandSender> { sender, context, value ->
                val id = context["id"]
                try {
                    val pid = context["pid"]
                    val args = value.split(' ')
                    ScheduleService.get(id).start(
                        pid = pid,
                        args = args
                    )
                    Lang.MODULE_SCHEDULE_COMMAND_START_SUCCESS.info(sender, id, pid, args)
                } catch (e: Exception) {
                    Lang.MODULE_SCHEDULE_COMMAND_START_FAILURE.error(sender, id, e.localizedMessage)
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
                    Lang.MODULE_SCHEDULE_COMMAND_PAUSE_ALL_SUCCESS.info(sender, id)
                } catch (e: Exception) {
                    Lang.MODULE_SCHEDULE_COMMAND_PAUSE_ALL_FAILURE.error(sender, id, e.localizedMessage)
                }
            }
        }.dynamic("pid") {
            suggest { ScheduleService.get(ctx["id"]).tasks.keys.toList() }
            execute<ProxyCommandSender> { sender, context, pid ->
                val id = context["id"]
                try {
                    ScheduleService.get(id).pause(pid)
                    Lang.MODULE_SCHEDULE_COMMAND_PAUSE_TASK_SUCCESS.info(sender, id, pid)
                } catch (e: Exception) {
                    Lang.MODULE_SCHEDULE_COMMAND_PAUSE_TASK_FAILURE.error(sender, id, pid, e.localizedMessage)
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
                    Lang.MODULE_SCHEDULE_COMMAND_RESUME_ALL_SUCCESS.info(sender, id)
                } catch (e: Exception) {
                    Lang.MODULE_SCHEDULE_COMMAND_RESUME_ALL_FAILURE.error(sender, id, e.localizedMessage)
                }
            }
        }.dynamic("pid") {
            suggest { ScheduleService.get(ctx["id"]).tasks.keys.toList() }
            execute<ProxyCommandSender> { sender, context, pid ->
                val id = context["id"]
                try {
                    ScheduleService.get(id).resume(pid)
                    Lang.MODULE_SCHEDULE_COMMAND_RESUME_TASK_SUCCESS.info(sender, id, pid)
                } catch (e: Exception) {
                    Lang.MODULE_SCHEDULE_COMMAND_RESUME_TASK_FAILURE.error(sender, id, pid, e.localizedMessage)
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
                    Lang.MODULE_SCHEDULE_COMMAND_STOP_ALL_SUCCESS.info(sender, id)
                } catch (e: Exception) {
                    Lang.MODULE_SCHEDULE_COMMAND_STOP_ALL_FAILURE.error(sender, id, e.localizedMessage)
                }
            }
        }.dynamic("pid") {
            suggest { ScheduleService.getOrNull(ctx["id"])?.tasks?.keys?.toList() }
            execute<ProxyCommandSender> { sender, context, pid ->
                val id = context["id"]
                try {
                    ScheduleService.get(id).stop(pid)
                    Lang.MODULE_SCHEDULE_COMMAND_STOP_TASK_SUCCESS.info(sender, id, pid)
                } catch (e: Exception) {
                    Lang.MODULE_SCHEDULE_COMMAND_STOP_TASK_FAILURE.error(sender, id, pid, e.localizedMessage)
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
            ScheduleService.reload(sender.withConsole())
        }
    }

    private fun showScheduleDetail(sender: ProxyCommandSender, id: String) {
        // 获取任务列表
        val tasks = if (id == "*") {
            ScheduleService.values().flatMap { it.tasks.values }
        } else {
            ScheduleService.get(id).tasks.values
        }

        val builder = Components.text(Lang.MODULE_SCHEDULE_COMMAND_DETAIL_HEADER.asText(sender))
        for (task in tasks) {
            builder.newLine()

            // 消息项
            val pid = task.pid
            val state = when (task.state) {
                TaskState.WAITING -> Lang.MODULE_SCHEDULE_TASK_STATE_WAITING.asText(sender)
                TaskState.RUNNING -> Lang.MODULE_SCHEDULE_TASK_STATE_RUNNING.asText(sender)
                TaskState.PAUSED -> Lang.MODULE_SCHEDULE_TASK_STATE_PAUSED.asText(sender)
                TaskState.TERMINATED -> Lang.MODULE_SCHEDULE_TASK_STATE_TERMINATED.asText(sender)
            }
            val message = Lang.MODULE_SCHEDULE_COMMAND_DETAIL_ITEM.asText(sender, task.id, pid, state, task.counter)
            builder.append(message)

            if (sender !is ProxyPlayer) {
                // 非玩家操作者不显示操作按钮
                continue
            }

            // 操作按钮
            val pause = Components
                .text(Lang.MODULE_SCHEDULE_TASK_OPERATION_PAUSE.asText(sender))
                .hoverText(Lang.MODULE_SCHEDULE_TASK_OPERATION_PAUSE_HOVER.asText(sender))
                .clickSuggestCommand("/vul schedule pause ${task.id} $pid")
            val resume = Components
                .text(Lang.MODULE_SCHEDULE_TASK_OPERATION_RESUME.asText(sender))
                .hoverText(Lang.MODULE_SCHEDULE_TASK_OPERATION_RESUME_HOVER.asText(sender))
                .clickSuggestCommand("/vul schedule resume ${task.id} $pid")
            val terminate = Components
                .text(Lang.MODULE_SCHEDULE_TASK_OPERATION_TERMINATE.asText(sender))
                .hoverText(Lang.MODULE_SCHEDULE_TASK_OPERATION_TERMINATE_HOVER.asText(sender))
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
        val footer = Lang.MODULE_SCHEDULE_COMMAND_DETAIL_FOOTER.asText(sender)
        if (footer.isNotEmpty()) {
            builder.newLine().append(footer)
        }

        // 发送消息
        if (sender is ProxyPlayer) {
            builder.sendTo(sender)
        } else {
            sender.sendMessage(builder.toLegacyText())
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