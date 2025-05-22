package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.common.platform.command.suggestPlayers
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import top.lanscarlos.vulpecula.common.message.*
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
                sender.info("module-script-command-run", id, sender.name, "[]")
                ScriptService.run(
                    id = id,
                    sender = sender,
                    onSuccess = {
                        sender.info("module-script-command-run-success", id, it.toString())
                    },
                    onFailure = { ex ->
                        sender.errorSync("module-script-command-run-failure", id)
                        sender.errorLiteralSync(ex.getActionMessage())
                        sender.errorLiteralSync(ex.getReasonMessage())
                        sender.errorLiteralSync(ex.getDetailMessage())
                    }
                )
            }

            dynamic("sender") {
                suggestPlayers(listOf("@NULL", "@SELF", "@CONSOLE"))
                execute<ProxyCommandSender> { sender, context, senderName ->
                    val id = context["id"]
                    val scriptSender = senderName.toSender(sender)
                    sender.info("module-script-command-run", id, scriptSender?.name ?: "null", "[]")
                    ScriptService.run(
                        id = id,
                        sender = scriptSender,
                        onSuccess = {
                            sender.info("module-script-command-run-success", id, it.toString())
                        },
                        onFailure = { ex ->
                            sender.errorSync("module-script-command-run-failure", id)
                            sender.errorLiteralSync(ex.getActionMessage())
                            sender.errorLiteralSync(ex.getReasonMessage())
                            sender.errorLiteralSync(ex.getDetailMessage())
                        }
                    )
                }

                dynamic("args") {
                    execute<ProxyCommandSender> { sender, context, value ->
                        val id = context["id"]
                        val scriptSender = context["sender"].toSender(sender)
                        val args = value.split(' ')
                        sender.info("module-script-command-run", id, scriptSender?.name ?: "null", args)
                        ScriptService.run(
                            id = id,
                            sender = scriptSender,
                            args = args,
                            onSuccess = {
                                sender.info("module-script-command-run-success", id, it.toString())
                            },
                            onFailure = { ex ->
                                sender.errorSync("module-script-command-run-failure", id)
                                sender.errorLiteralSync(ex.getActionMessage())
                                sender.errorLiteralSync(ex.getReasonMessage())
                                sender.errorLiteralSync(ex.getDetailMessage())
                            }
                        )
                    }
                }
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
                ScriptService.run(
                    id = id,
                    sender = sender
                )
            }

            dynamic("sender") {
                suggestPlayers(listOf("@NULL", "@SELF", "@CONSOLE"))
                execute<ProxyCommandSender> { sender, context, senderName ->
                    val id = context["id"]
                    val scriptSender = senderName.toSender(sender)
                    ScriptService.run(
                        id = id,
                        sender = scriptSender
                    )
                }

                dynamic("args") {
                    execute<ProxyCommandSender> { sender, context, value ->
                        val id = context["id"]
                        val scriptSender = context["sender"].toSender(sender)
                        val args = value.split(' ')
                        ScriptService.run(
                            id = id,
                            sender = scriptSender,
                            args = args
                        )
                    }
                }
            }
        }
    }

    private val stop: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScriptService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                ScriptService.stop(id)
                sender.info("module-script-command-stop", id)
            }
        }
    }

    private val task: CommandComponent.() -> Unit = {
        literal("stop") {
            dynamic("pid") {
                suggest { ScriptService.getTaskKeys().map { it.toString() } }
                execute<ProxyCommandSender> { sender, _, pid ->
                    ScriptService.stop(pid.toLong())
                    sender.info("module-script-command-task-stop", pid)
                }
            }
        }
        literal("list") {
            execute<ProxyCommandSender> { sender, _, _ ->
                sender.info("module-script-command-task-list-header")
                for (task in ScriptService.getTaskValues()) {
                    val pid = task.pid
                    val script = task.script.id
                    val startTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(Date(task.startTime))
                    sender.info("module-script-command-task-list-item", pid, script, startTime)
                }
                val footer = MessageService.asInfo("module-script-command-task-list-footer")
                if (footer.isNotBlank()) {
                    sender.sendMessage(footer)
                }
            }
        }
    }

    private val reload: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            ScriptService.reload(sender)
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