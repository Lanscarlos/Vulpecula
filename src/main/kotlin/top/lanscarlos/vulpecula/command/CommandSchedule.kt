package top.lanscarlos.vulpecula.command

import org.bukkit.command.CommandSender
import taboolib.common.platform.command.component.CommandComponent
import top.lanscarlos.vulpecula.internal.ScheduleTask
import top.lanscarlos.vulpecula.utils.sendSyncLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.command
 *
 * @author Lanscarlos
 * @since 2023-02-06 17:37
 */
object CommandSchedule {

    val main: CommandComponent.() -> Unit = {
        literal("run", literal = run)
        literal("stop", literal = stop)
        literal("list", literal = list)
        literal("detail", literal = detail)
    }

    /**
     * vul schedule run taskId args?...
     * */
    val run: CommandComponent.() -> Unit = {
        dynamic("taskId") {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
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
                    val taskId = context["taskId"]
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
    val stop: CommandComponent.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            try {
                ScheduleTask.cache.values.forEach { it.terminate() }
                sender.sendSyncLang("Schedule-Stop-All-Succeeded")
            } catch (e: Exception) {
                sender.sendSyncLang("Schedule-Stop-All-Failed", e.localizedMessage)
                e.printStackTrace()
            }
        }

        dynamic("taskId", optional = true) {
            suggestion<CommandSender> { _, _ ->
                ScheduleTask.cache.values.mapNotNull {
                    if (it.isRunning) it.id else null
                }
            }
            execute<CommandSender> { sender, _, taskId ->
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

    /**
     * vul schedule list
     * */
    val list: CommandComponent.() -> Unit = {
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
    val detail: CommandComponent.() -> Unit = {
        dynamic("taskId") {
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