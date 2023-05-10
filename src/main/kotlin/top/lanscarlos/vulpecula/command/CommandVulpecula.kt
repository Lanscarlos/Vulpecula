package top.lanscarlos.vulpecula.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.common.platform.function.adaptCommandSender
import taboolib.expansion.createHelper
import taboolib.module.kether.printKetherErrorMessage
import top.lanscarlos.vulpecula.VulpeculaContext
import top.lanscarlos.vulpecula.bacikal.action.internal.ActionUnicode
import top.lanscarlos.vulpecula.internal.*
import top.lanscarlos.vulpecula.utils.runActions
import top.lanscarlos.vulpecula.utils.toKetherScript

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.command
 *
 * @author Lanscarlos
 * @since 2022-10-17 20:39
 */
@CommandHeader(
    name = "vulpecula",
    aliases = ["vul"],
    permission = "vulpecula.command",
    permissionDefault = PermissionDefault.OP
)
object CommandVulpecula {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val eval = subCommand {
        dynamic {
            execute<CommandSender> { sender, _, content ->
                try {
                    val script = if (content.startsWith("def")) {
                        content
                    } else {
                        "def main = { $content }"
                    }
                    script.toKetherScript().runActions {
                        this.sender = adaptCommandSender(sender)
                        if (sender is Player) {
                            set("player", sender)
                            set("hand", sender.equipment?.itemInMainHand)
                        }
                    }.thenAccept {
                        sender.sendMessage(" §5§l‹ ›§r §7Result: §f$it")
                    }
                } catch (e: Exception) {
                    e.printKetherErrorMessage()
                }
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            val messages = VulpeculaContext.load()
            (sender as? Player)?.sendMessage(*messages.toTypedArray())
        }

        dynamic("modules", true) {
            suggestUncheck {
                listOf(
                    "config",
                    "command", "schedule",
                    "dispatcher", "handler", "listen-mapping",
                    "script-source", "script-compiled"
                )
            }
            execute<CommandSender> { sender, _, argument ->

                // 排序，防止载入顺序导致报错
                val modules = argument.split(' ').sortedBy {
                    when (it) {
                        "config" -> 0
                        "listen-mapping" -> 1
                        "unicode" -> 2
                        "script-source" -> 3
                        "script-compiled" -> 4
                        "dispatcher" -> 5
                        "handler" -> 6
                        "command" -> 7
                        "schedule" -> 8
                        else -> 9
                    }
                }

                val messages = mutableListOf<String>()
                for (module in modules) {
                    messages += when (module) {
                        "config" -> VulpeculaContext.loadConfig()
                        "listen-mapping" -> EventHandler.load()
                        "unicode" -> if (ActionUnicode.enable) ActionUnicode.load() else continue
                        "script-source" -> VulScript.load()
                        "script-compiled" -> ScriptWorkspace.load()
                        "dispatcher" -> EventDispatcher.load()
                        "handler" -> EventHandler.load()
                        "command" -> CustomCommand.load()
                        "schedule" -> ScheduleTask.load(false)
                        else -> continue
                    }
                }

                if ("dispatcher" in modules) {
                    EventDispatcher.postLoad()
                }

                (sender as? Player)?.sendMessage(*messages.toTypedArray())
            }
        }
    }

    @CommandBody
    val script = subCommand(CommandScript.main)

    @CommandBody
    val schedule = subCommand(CommandSchedule.main)

    @CommandBody
    val dispatcher = subCommand(CommandDispatcher.main)

    @CommandBody(permission = "vulpecula.command.util")
    val util = subCommand {
        CommandUtilTiming.main(this)
    }

}