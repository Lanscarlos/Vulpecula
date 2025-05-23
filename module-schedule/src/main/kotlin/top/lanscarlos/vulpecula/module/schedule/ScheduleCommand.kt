package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.common.platform.command.suggestPlayers
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers

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
        literal("stop", literal = stop)
        literal("reload", literal = reload)
    }

    private val start: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScheduleService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                ScheduleService.get(id).start()
                sender.sendMessage("schedule $id successfully started.")
            }
        }.dynamic("sender") {
            suggestPlayers(listOf("@NULL", "@SELF", "@CONSOLE"))
            execute<ProxyCommandSender> { sender, context, value ->
                val id = context["id"]
                val runtimeSender = value.toSender(sender)
                ScheduleService.get(id).start(
                    sender = runtimeSender
                )
                sender.sendMessage("schedule $id successfully started.")
            }
        }.dynamic("args") {
            execute<ProxyCommandSender> { sender, context, value ->
                val id = context["id"]
                val runtimeSender = context["sender"].toSender(sender)
                val args = value.split(' ')
                val pid = args.find { it.startsWith("--pid=") }?.substring(5) ?: "~"
                ScheduleService.get(id).start(
                    pid = pid,
                    sender = runtimeSender,
                    args = args.filter { !it.startsWith("--pid=") } // 去除内置参数
                )
                sender.sendMessage("schedule $id successfully started. with pid: $pid.")
            }
        }
    }

    private val stop: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { ScheduleService.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                ScheduleService.get(id).stop("*")
                sender.sendMessage("schedule $id successfully stopped.")
            }
        }.dynamic("pid") {
            suggest { ScheduleService.get(ctx["id"]).tasks.keys.toList() }
            execute<ProxyCommandSender> { sender, context, pid ->
                val id = context["id"]
                ScheduleService.get(id).stop(pid)
                sender.sendMessage("schedule $id with pid $pid successfully stopped.")
            }
        }
    }

    private val reload: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            ScheduleService.reload(sender)
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