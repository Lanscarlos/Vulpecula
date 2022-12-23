package top.lanscarlos.vulpecula.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.suggestPlayers
import taboolib.module.chat.colored
import taboolib.module.kether.Kether
import taboolib.platform.util.sendLang
import top.lanscarlos.vulpecula.script.VulWorkspace

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.command
 *
 * @author Lanscarlos
 * @since 2022-12-23 20:42
 */
object CommandScript {

    val main: CommandBuilder.CommandComponent.() -> Unit = {
        literal("run", literal = run)
        literal("stop", literal = stop)
        literal("list", literal = list)
        literal("reload", literal = reload)
        literal("debug", literal = debug)
    }

    val run: CommandBuilder.CommandComponent.() -> Unit = {
        dynamic("file") {
            suggestion<CommandSender> { _, _ ->
                VulWorkspace.scripts.map { it.value.id }
            }
            execute<CommandSender> { sender, _, file ->
                try {
                    VulWorkspace.runScript(file)
                    sender.sendLang("Script-Run-Succeeded", file)
                } catch (e: Exception) {
                    sender.sendLang("Script-Run-Failed", file, e.localizedMessage)
                    e.printStackTrace()
                }
            }

            dynamic("viewer", optional = true) {
                suggestPlayers(false)
                execute<CommandSender> { sender, context, viewer ->
                    val file = context.argument(-1)
                    try {
                        VulWorkspace.runScript(file, viewer)
                        sender.sendLang("Script-Run-Succeeded", file)
                    } catch (e: Exception) {
                        sender.sendLang("Script-Run-Failed", file, e.localizedMessage)
                        e.printStackTrace()
                    }
                }

                dynamic("args", optional = true) {
                    execute<CommandSender> { sender, context, args ->
                        val file = context.argument(-2)
                        val viewer = context.argument(-1)
                        try {
                            VulWorkspace.runScript(file, viewer, args.split(' ').toTypedArray())
                            sender.sendLang("Script-Run-Succeeded", file)
                        } catch (e: Exception) {
                            sender.sendLang("Script-Run-Failed", file, e.localizedMessage)
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    val stop: CommandBuilder.CommandComponent.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            try {
                VulWorkspace.terminateAllScript()
                sender.sendLang("Script-Stop-All-Succeeded")
            } catch (e: Exception) {
                sender.sendLang("Script-Stop-All-Failed", e.localizedMessage)
                e.printStackTrace()
            }
        }

        dynamic("file", optional = true) {
            suggestion<CommandSender> { _, _ ->
                VulWorkspace.scripts.map { it.value.id }
            }
            execute<CommandSender> { sender, _, file ->
                try {
                    VulWorkspace.terminateScript(file)
                    sender.sendLang("Script-Stop-Succeeded", file)
                } catch (e: Exception) {
                    sender.sendLang("Script-Stop-Failed", file, e.localizedMessage)
                    e.printStackTrace()
                }
            }
        }
    }

    val list: CommandBuilder.CommandComponent.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            sender.sendLang(
                "Script-List",
                VulWorkspace.scripts.map { it.value.id }.joinToString(", "),
                VulWorkspace.getRunningScript().joinToString(", ") { it.id }
            )
        }
    }

    val reload: CommandBuilder.CommandComponent.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            VulWorkspace.terminateAllScript()
            VulWorkspace.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

    val debug: CommandBuilder.CommandComponent.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&8[&3Vul&bpecula&8] &e调试 &8| &7RegisteredActions:".colored())
            Kether.scriptRegistry.registeredNamespace.forEach {
                sender.sendMessage("&8[&3Vul&bpecula&8] &e调试 &8| &7  ${it}: &r${Kether.scriptRegistry.getRegisteredActions(it)}".colored())
            }
        }
    }
}