package top.lanscarlos.vulpecula.bacikal.command

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import taboolib.module.chat.colored
import taboolib.module.kether.*
import java.nio.charset.StandardCharsets

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.command
 *
 * @author Lanscarlos
 * @since 2024-05-14 15:56
 */
object EvalCommand {

    @CommandBody
    val eval = subCommand {
        dynamic {
            execute<ProxyCommandSender> { sender, _, content ->
                if (content == "debug") {
                    sender.sendMessage("&8[&3Vul&bpecula&8] &e调试 &8| &7RegisteredActions:".colored())
                    Kether.scriptRegistry.registeredNamespace.forEach {
                        sender.sendMessage("&8[&3Vul&bpecula&8] &e调试 &8| &7  ${it}: &r${Kether.scriptRegistry.getRegisteredActions(it)}".colored())
                    }
                    return@execute
                }

                try {
                    val source = if (!content.startsWith("def")) "def main = { $content }" else content
                    val script = KetherScriptLoader().load(
                        ScriptService,
                        "bacikal_eval",
                        source.toByteArray(StandardCharsets.UTF_8),
                        listOf("vulpecula")
                    )
                    ScriptContext.create(script).apply {
                        this.sender = sender
                        (sender.origin as? Player)?.let { player ->
                            set("player", player)
                            set("hand", player.equipment?.itemInMainHand)
                        }
                    }.runActions().thenAccept {
                        sender.sendMessage(" §5§l‹ ›§r §7Result: §f$it")
                    }

//                    bacikalSimpleQuest("eval") {
//                        appendContent(content)
//                    }.runActions {
//                        this.sender = sender
//                        (sender.origin as? Player)?.let { player ->
//                            setVariable("player", player)
//                            setVariable("hand", player.equipment?.itemInMainHand)
//                        }
//                    }.thenAccept {
//                        sender.sendMessage(" §5§l‹ ›§r §7Result: §f$it")
//                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.printKetherErrorMessage(true)
                }
            }
        }
    }

}