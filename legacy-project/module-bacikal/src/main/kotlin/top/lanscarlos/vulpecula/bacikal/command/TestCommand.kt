package top.lanscarlos.vulpecula.bacikal.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.warning
import taboolib.module.kether.printKetherErrorMessage
import top.lanscarlos.vulpecula.bacikal.Bacikal.buildQuest
import top.lanscarlos.vulpecula.bacikal.quest.KetherQuestExecutor
import top.lanscarlos.vulpecula.config.toDynamicConfig
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.command
 *
 * @author Lanscarlos
 * @since 2024-05-18 18:44
 */
object TestCommand {

    @CommandBody
    val test = subCommand {
        dynamic("path") {
            execute<ProxyCommandSender> { sender, _, path ->
                try {
                    val file = File(getDataFolder(), "test.yml")
                    if (!file.exists()) {
                        warning("文件不存在...")
                        return@execute
                    }
                    sender.sendMessage("正在编译...")
                    val config = file.toDynamicConfig()
                    val quest = buildQuest(path) {
                        it.appendContent(config.read(path))
                    }
                    KetherQuestExecutor.execute(quest, "main") {
                        this.sender = sender
                    }.thenAccept {
                        sender.sendMessage(" §5§l‹ ›§r §7Result: §f$it")
                    }
                } catch (ex: Exception) {
                    ex.printKetherErrorMessage(true)
                }
            }
        }
    }

}