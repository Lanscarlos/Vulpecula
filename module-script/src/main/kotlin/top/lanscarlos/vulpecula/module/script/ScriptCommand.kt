package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import taboolib.module.lang.sendLang

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
        literal("stop", literal = stop)
        literal("view")
    }

    private val run: CommandComponent.() -> Unit = {
        dynamic("id") {
            execute<ProxyCommandSender> { sender, _, id ->
                ScriptService.run(id, sender, emptyMap())
                sender.sendLang("module-script-command-run", id, sender.name, "[]")
            }
            dynamic("sender") {
                execute<ProxyCommandSender> { sender, context, senderName ->
                    val id = context["id"]
                    val scriptSender = senderName.toSender(sender)
                    ScriptService.run(id, scriptSender, emptyMap())
                    sender.sendLang("module-script-command-run", id, scriptSender?.name ?: "null", "[]")
                }

                dynamic("args") {
                    execute<ProxyCommandSender> { sender, context, value ->
                        val id = context["id"]
                        val scriptSender = context["sender"].toSender(sender)
                        val args = value.split(' ')
                        val wrappedArgs = mutableMapOf<String, Any>()
                        wrappedArgs["args"] = args
                        for ((index, arg) in args.withIndex()) {
                            wrappedArgs["arg$index"] = arg
                        }
                        ScriptService.run(id, scriptSender, wrappedArgs)
                        sender.sendLang("module-script-command-run", id, scriptSender?.name ?: "null", args)
                    }
                }
            }
        }
    }

    private val stop: CommandComponent.() -> Unit = {
        literal("script") {
            dynamic("id") {
                execute<ProxyCommandSender> { sender, _, id ->
                    ScriptService.stop(id)
                    sender.sendLang("module-script-command-stop", id)
                }
            }
        }
        literal("task") {
            dynamic("pid") {
                execute<ProxyCommandSender> { sender, _, pid ->
                    ScriptService.stop(pid.toLong())
                    sender.sendLang("module-script-command-stop-task", pid)
                }
            }
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