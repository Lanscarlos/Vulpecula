package top.lanscarlos.vulpecula.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import top.lanscarlos.vulpecula.internal.ScheduleTask
import top.lanscarlos.vulpecula.script.ScriptWorkspace
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
        literal("run", literal = CommandScript.run)
        literal("stop", literal = CommandScript.stop)
        literal("compile", "build", literal = CommandScript.compile)
        literal("list", literal = CommandScript.list)
        literal("reload", literal = CommandScript.reload)
        literal("debug", literal = CommandScript.debug)
    }

    /**
     * asd
     * */
    val run: CommandComponent.() -> Unit = {
        dynamic(comment = "taskId") {
            suggestion<CommandSender> { _, _ ->
                ScheduleTask.cache.keys.toList()
            }
            execute<CommandSender> { sender, _, taskId ->
                try {
                    val task = ScheduleTask.get(taskId)
                    if (task != null) {
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

}