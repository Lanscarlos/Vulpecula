package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.common.platform.command.suggestPlayers
import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.common.platform.function.onlinePlayers
import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.script.exception.ScriptExecuteException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025/4/25 17:25
 */
object ScriptCommand {

    @CommandBody
    val script = subCommand {
        literal("run", literal = run)
        literal("run-silent", literal = runSilent)
        literal("stop", literal = stop)
        literal("task", literal = task)
        literal("reload", literal = reload)
    }

    private val run: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScriptService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                runScript(sender, id, sender, emptyList())
            }
        }.dynamic("sender") {
            suggestPlayers(listOf("@NULL", "@SELF", "@CONSOLE"))
            execute<ProxyCommandSender> { sender, context, value ->
                val id = context["id"]
                val scriptSender = value.toSender(sender)
                runScript(sender, id, scriptSender, emptyList())
            }
        }.dynamic("args") {
            execute<ProxyCommandSender> { sender, context, value ->
                val id = context["id"]
                val scriptSender = context["sender"].toSender(sender)
                val args = value.split(' ')
                runScript(sender, id, scriptSender, args)
            }
        }
    }

    /**
     * 静默运行脚本
     * */
    private val runSilent: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScriptService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                runScriptSilent(sender, id, sender, emptyList())
            }
        }.dynamic("sender") {
            suggestPlayers(listOf("@NULL", "@SELF", "@CONSOLE"))
            execute<ProxyCommandSender> { sender, context, value ->
                val id = context["id"]
                val scriptSender = value.toSender(sender)
                runScriptSilent(sender, id, scriptSender, emptyList())
            }
        }.dynamic("args") {
            execute<ProxyCommandSender> { sender, context, value ->
                val id = context["id"]
                val scriptSender = context["sender"].toSender(sender)
                val args = value.split(' ')
                runScriptSilent(sender, id, scriptSender, args)
            }
        }
    }

    private val stop: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScriptService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                ScriptService.stop(id)
                Lang.MODULE_SCRIPT_STOP.info(sender, id)
            }
        }
    }

    private val task: CommandComponent.() -> Unit = {
        literal("stop") {
            dynamic("pid") {
                suggest { ScriptService.getTaskKeys().map { it.toString() } }
                execute<ProxyCommandSender> { sender, _, pid ->
                    ScriptService.stop(pid.toLong())
                    Lang.MODULE_SCRIPT_STOP_TASK.info(sender, pid)
                }
            }
        }
        literal("list") {
            execute<ProxyCommandSender> { sender, _, _ ->
                val scriptTasks = ScriptService.getTaskValues()
                if (scriptTasks.isEmpty()) {
                    Lang.MODULE_SCRIPT_TASK_EMPTY.info(sender)
                    return@execute
                }
                Lang.MODULE_SCRIPT_TASK_INFO.info(sender)
                for ((scriptId, tasks) in scriptTasks.groupBy { it.script.id }) {
                    Lang.MODULE_SCRIPT_TASK_BODY.info(sender, scriptId, tasks.size)
                    for ((index, task) in tasks.withIndex()) {
                        val pid = String.format("%6d", task.pid)
                        val startTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(Date(task.startTime))
                        if (index < tasks.size - 1) {
                            Lang.MODULE_SCRIPT_TASK_BRANCH.info(sender, pid, startTime)
                        } else {
                            Lang.MODULE_SCRIPT_TASK_BRANCH_END.info(sender, pid, startTime)
                        }
                    }
                }
            }
        }
    }

    private val reload: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            ScriptService.reload(sender)
        }
    }

    private fun runScript(sender: ProxyCommandSender, id: String, scriptSender: ProxyCommandSender?, args: List<String>) {
        val task = try {
            Lang.MODULE_SCRIPT_RUN_INFO.info(sender, id, scriptSender?.name ?: "null", args)
            ScriptService.run(
                id = id,
                sender = scriptSender,
                args = args
            )
        } catch (e: Exception) {
            val cause = when (e) {
                is ScriptExecuteException -> e.cause
                else -> e
            }
            val message = (cause as? AbstractLocalizedException)?.getLocalizedMessage(sender) ?: cause.localizedMessage
            Lang.MODULE_SCRIPT_RUN_FAILURE.error(sender, id, message)
            null
        }
        task?.onSuccess {
            Lang.MODULE_SCRIPT_RUN_SUCCESS.info(sender, id, it.toString())
        }
        task?.onFailure { ex ->
            Lang.MODULE_SCRIPT_RUN_FAILURE.error(sender, id, "")
            ex.notice(sender)
        }
    }

    private fun runScriptSilent(sender: ProxyCommandSender, id: String, scriptSender: ProxyCommandSender?, args: List<String>) {
        val task = try {
            ScriptService.run(
                id = id,
                sender = scriptSender,
                args = args
            )
        } catch (e: Exception) {
            val message = (e as? AbstractLocalizedException)?.getLocalizedMessage(sender) ?: e.localizedMessage
            Lang.MODULE_SCRIPT_RUN_FAILURE.error(sender, id, message)
            null
        }
        task?.onFailure { ex ->
            Lang.MODULE_SCRIPT_RUN_FAILURE.error(sender, id, "")
            ex.notice(sender)
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