package top.lanscarlos.vulpecula.legacy.command

import org.bukkit.command.CommandSender
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.suggest
import taboolib.common.platform.command.suggestUncheck
import top.lanscarlos.vulpecula.legacy.internal.ScheduleTask
import top.lanscarlos.vulpecula.legacy.utils.sendSyncLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.command
 *
 * @author Lanscarlos
 * @since 2023-02-06 17:37
 */
object CommandSchedule {

    internal val main: CommandComponent.() -> Unit = {
        literal("run", literal = run)
        literal("stop", literal = stop)
        literal("list", literal = list)
        literal("detail", literal = detail)
    }

    /**
     * vul schedule run taskId args?...
     * */
    private val run: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggestUncheck {
                ScheduleTask.cache.values.mapNotNull {
                    if (it.isStopped) it.id else null
                }
            }
            execute<CommandSender> { sender, _, taskId ->
                try {
                    val task = ScheduleTask.get(taskId)
                    if (task != null) {
                        if (task.isRunning) {
                            sender.sendSyncLang("Schedule-Run-Already", taskId)
                            return@execute
                        }

                        task.runTask()
                        sender.sendSyncLang("Schedule-Run-Succeeded", taskId)
                    } else {
                        sender.sendSyncLang("Schedule-Not-Found", taskId)
                    }
                } catch (e: Exception) {
                    sender.sendSyncLang("Schedule-Run-Failed", taskId, e.localizedMessage)
                    e.printStackTrace()
                }
            }

            dynamic("args", optional = true) {
                execute<CommandSender> { sender, context, argument ->
                    val taskId = context["id"]
                    val args = argument.split(' ')
                    val silent = args.contains("--silent")

                    try {
                        val task = ScheduleTask.get(taskId)
                        if (task != null) {
                            if (task.isRunning && !args.contains("--force")) {
                                sender.sendSyncLang("Schedule-Run-Already", taskId)
                                return@execute
                            }

                            task.runTask(args.toTypedArray())

                            // 静默通知
                            sender.sendSyncLang(silent, "Schedule-Run-Succeeded", taskId)
                        } else {
                            sender.sendSyncLang(silent, "Schedule-Not-Found", taskId)
                        }
                    } catch (e: Exception) {
                        // 静默通知
                        sender.sendSyncLang(silent, "Schedule-Run-Failed", taskId, e.localizedMessage)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * vul schedule stop taskId?
     * */
    private val stop: CommandComponent.() -> Unit = {
        dynamic("id", optional = true) {
            suggest {
                ScheduleTask.cache.values.mapNotNull {
                    if (it.isRunning) it.id else null
                }.plus("*")
            }
            execute<CommandSender> { sender, _, taskId ->
                if (taskId == "*") {
                    // 终止所有日程模块
                    try {
                        ScheduleTask.cache.values.forEach { it.terminate() }
                        sender.sendSyncLang("Schedule-Stop-All-Succeeded")
                    } catch (e: Exception) {
                        sender.sendSyncLang("Schedule-Stop-All-Failed", e.localizedMessage)
                        e.printStackTrace()
                    }
                } else {
                    try {
                        val task = ScheduleTask.get(taskId)
                        if (task != null) {
                            if (task.isStopped) {
                                sender.sendSyncLang("Schedule-Run-Already", taskId)
                                return@execute
                            }

                            task.terminate()
                            sender.sendSyncLang("Schedule-Stop-Succeeded", taskId)
                        } else {
                            sender.sendSyncLang("Schedule-Not-Found", taskId)
                        }
                    } catch (e: Exception) {
                        sender.sendSyncLang("Schedule-Stop-Failed", taskId, e.localizedMessage)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * vul schedule list
     * */
    private val list: CommandComponent.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            sender.sendSyncLang(
                "Schedule-List",
                ScheduleTask.cache.map { it.value.id }.joinToString(", "),
                ScheduleTask.cache.values.filter { it.isRunning }.joinToString(", ") { it.id }
            )
        }
    }

    /**
     * vul schedule detail taskId
     * */
    private val detail: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggestion<CommandSender> { _, _ ->
                ScheduleTask.cache.keys.toList()
            }
            execute<CommandSender> { sender, _, taskId ->
                val task = ScheduleTask.get(taskId)
                if (task != null) {
                    sender.sendSyncLang(
                        "Schedule-Detail",
                        taskId,
                        if (task.isRunning) "§aRUNNING" else "§cSTOPPED",
                        task.async, task.delay, task.period, task.startDate, task.endDate
                    )
                } else {
                    sender.sendSyncLang("Schedule-Not-Found", taskId)
                }
            }
        }
    }

}