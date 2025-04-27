package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers

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
            }
            dynamic("sender") {
                execute<ProxyCommandSender> { sender, context, senderName ->
                    val id = context["id"]
                    val scriptSender = when {
                        senderName.equals("@NULL", true) -> null
                        senderName.equals("@SELF", true) -> sender
                        senderName.equals("@CONSOLE", true) -> console()
                        else -> onlinePlayers().find { it.name.equals(senderName, true) }
                            ?: error("No sender found for $senderName.")
                    }
                    ScriptService.run(id, scriptSender, emptyMap())
                }

                dynamic("args") {
                    execute<ProxyCommandSender> { sender, context, value ->
                        val id = context["id"]
                        val senderName = context["sender"]
                        val scriptSender = when {
                            senderName.equals("@NULL", true) -> null
                            senderName.equals("@SELF", true) -> sender
                            senderName.equals("@CONSOLE", true) -> console()
                            else -> onlinePlayers().find { it.name.equals(senderName, true) }
                                ?: error("No sender found for $senderName.")
                        }
                        val args = value.split(' ')
                        val wrappedArgs = mutableMapOf<String, Any>()
                        wrappedArgs["args"] = args
                        for ((index, arg) in args.withIndex()) {
                            wrappedArgs["arg$index"] = arg
                        }
                        ScriptService.run(id, scriptSender, wrappedArgs)
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
                }
            }
        }
        literal("task") {
            dynamic("pid") {
                execute<ProxyCommandSender> { sender, _, pid ->
                    ScriptService.stop(pid.toLong())
                }
            }
        }
    }

}