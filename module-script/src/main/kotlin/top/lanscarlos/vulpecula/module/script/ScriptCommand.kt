package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.common.platform.command.suggestPlayers
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import top.lanscarlos.vulpecula.common.utils.asLang
import java.text.SimpleDateFormat
import java.util.*

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
                sender.info { asLang("module-script-command-stop", id) }
            }
        }
    }

    private val task: CommandComponent.() -> Unit = {
        literal("stop") {
            dynamic("pid") {
                suggest { ScriptService.getTaskKeys().map { it.toString() } }
                execute<ProxyCommandSender> { sender, _, pid ->
                    ScriptService.stop(pid.toLong())
                    sender.info { asLang("module-script-command-task-stop", pid) }
                }
            }
        }
        literal("list") {
            execute<ProxyCommandSender> { sender, _, _ ->
//                val builder = StringBuilder(asLang("module-script-command-task-list-header"))
                for (task in ScriptService.getTaskValues()) {
                    val pid = task.pid
                    val script = task.script.id
                    val startTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(Date(task.startTime))
                    val message = asLang("module-script-command-task-list-item", pid, script, startTime)
//                    builder.append('\n').append(message)
                }
//                sender.info { builder.toString() }
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
            ScriptService.run(
                id = id,
                sender = scriptSender,
                args = args
            ).also {
                sender.info { asLang("module-script-command-run", id, scriptSender?.name ?: "null", args) }
            }
        } catch (e: Exception) {
            sender.error(sync = true) { e.localizedMessage }
            null
        }
        task?.onSuccess {
            sender.info { asLang("module-script-command-run-success", id, it.toString()) }
        }
        task?.onFailure { ex ->
            sender.error(sync = true) { asLang("module-script-command-run-failure", id) }
            ex.printLocalizedMessage(sender, ScriptService.name)
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
            console().error(sync = true) { e.localizedMessage }
            null
        }
        task?.onFailure { ex ->
            console().error(sync = true) { asLang("module-script-command-run-failure", id) }
            ex.printLocalizedMessage(sender, ScriptService.name)
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