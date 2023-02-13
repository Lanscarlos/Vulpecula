package top.lanscarlos.vulpecula.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.suggestPlayers
import taboolib.module.chat.colored
import taboolib.module.kether.Kether
import top.lanscarlos.vulpecula.internal.VulScript
import top.lanscarlos.vulpecula.internal.ScriptWorkspace
import top.lanscarlos.vulpecula.utils.sendSyncLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.command
 *
 * @author Lanscarlos
 * @since 2022-12-23 20:42
 */
object CommandScript {

    val main: CommandComponent.() -> Unit = {
        literal("run", literal = run)
        literal("stop", literal = stop)
        literal("compile", "build", literal = compile)
        literal("list", literal = list)
        literal("reload", literal = reload)
        literal("debug", literal = debug)
    }

    val run: CommandComponent.() -> Unit = {
        dynamic("file") {
            suggestion<CommandSender> { _, _ ->
                ScriptWorkspace.scripts.map { it.value.id }
            }
            execute<CommandSender> { sender, _, file ->
                try {
                    ScriptWorkspace.runScript(file)
                    sender.sendSyncLang("Script-Run-Succeeded", file)
                } catch (e: Exception) {
                    sender.sendSyncLang("Script-Run-Failed", file, e.localizedMessage)
                    e.printStackTrace()
                }
            }

            dynamic("viewer", optional = true) {
                suggestPlayers(emptyList())
                execute<CommandSender> { sender, context, viewer ->
                    val file = context["file"]
                    try {
                        ScriptWorkspace.runScript(file, Bukkit.getPlayerExact(viewer) ?: sender)
                        sender.sendSyncLang("Script-Run-Succeeded", file)
                    } catch (e: Exception) {
                        sender.sendSyncLang("Script-Run-Failed", file, e.localizedMessage)
                        e.printStackTrace()
                    }
                }

                dynamic("args", optional = true) {
                    execute<CommandSender> { sender, context, argument ->
                        val file = context["file"]
                        val viewer = context["viewer"].let {
                            if (it.equals("@Self", true)) {
                                // 以自身为执行者
                                sender
                            } else {
                                // 以指定玩家为执行者
                                Bukkit.getPlayerExact(it) ?: sender
                            }
                        }
                        val args = argument.split(' ')
                        val silent = args.contains("--silent")

                        try {
                            ScriptWorkspace.runScript(file, viewer, args.toTypedArray())?.let {
                                // 静默通知
                                sender.sendSyncLang(silent, "Script-Run-Succeeded", file)
                            } ?: sender.sendSyncLang(silent, "Script-Not-Found", file)
                        } catch (e: Exception) {
                            // 静默通知
                            sender.sendSyncLang(silent, "Script-Run-Failed", file, e.localizedMessage)
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    val stop: CommandComponent.() -> Unit = {
        dynamic("file", optional = true) {
            suggestion<CommandSender> { _, _ ->
                ScriptWorkspace.scripts.map { it.value.id }.plus("*")
            }
            execute<CommandSender> { sender, _, file ->
                if (file == "*") {
                    try {
                        ScriptWorkspace.terminateAllScript()
                        sender.sendSyncLang("Script-Stop-All-Succeeded")
                    } catch (e: Exception) {
                        sender.sendSyncLang("Script-Stop-All-Failed", e.localizedMessage)
                        e.printStackTrace()
                    }
                } else {
                    try {
                        ScriptWorkspace.terminateScript(file)
                        sender.sendSyncLang("Script-Stop-Succeeded", file)
                    } catch (e: Exception) {
                        sender.sendSyncLang("Script-Stop-Failed", file, e.localizedMessage)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    val compile: CommandComponent.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            try {
                VulScript.getAll().forEach { it.compileScript() }
                sender.sendSyncLang("Script-Compile-Command-All-Succeeded")
            } catch (e: Exception) {
                sender.sendSyncLang("Script-Compile-Command-All-Failed", e.localizedMessage)
                e.printStackTrace()
            }
        }

        dynamic("file", optional = true) {
            suggestion<CommandSender> { _, _ ->
                VulScript.getAll().map { it.id }
            }
            execute<CommandSender> { sender, _, file ->
                try {
                    VulScript.get(file)?.compileScript()
                    sender.sendSyncLang("Script-Compile-Command-Succeeded", file)
                } catch (e: Exception) {
                    sender.sendSyncLang("Script-Compile-Command-Failed", file, e.localizedMessage)
                    e.printStackTrace()
                }
            }
        }
    }

    val list: CommandComponent.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            sender.sendSyncLang(
                "Script-List",
                ScriptWorkspace.scripts.map { it.value.id }.joinToString(", "),
                ScriptWorkspace.getRunningScript().joinToString(", ") { it.id }
            )
        }
    }

    val reload: CommandComponent.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            ScriptWorkspace.terminateAllScript()
            VulScript.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
            ScriptWorkspace.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

    val debug: CommandComponent.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&8[&3Vul&bpecula&8] &e调试 &8| &7RegisteredActions:".colored())
            Kether.scriptRegistry.registeredNamespace.forEach {
                sender.sendMessage("&8[&3Vul&bpecula&8] &e调试 &8| &7  ${it}: &r${Kether.scriptRegistry.getRegisteredActions(it)}".colored())
            }
        }
    }
}