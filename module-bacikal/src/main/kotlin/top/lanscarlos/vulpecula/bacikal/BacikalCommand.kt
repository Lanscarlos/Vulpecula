package top.lanscarlos.vulpecula.bacikal

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.info
import taboolib.module.kether.printKetherErrorMessage
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuestCompiler
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuestExecutor

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2024-11-22 17:17
 */
object BacikalCommand {

    @CommandBody
    val eval = subCommand {
        dynamic("content") {
             execute<ProxyCommandSender> { sender, _, content ->
                 try {
                     val source = if (!content.startsWith("def")) "def main = { $content }" else content
                     val quest = BacikalQuestCompiler.compile(source, "eval", emptyList())
                     BacikalQuestExecutor.execute(quest, "main", sender, emptyMap()).handle { result, ex ->
                         if (ex != null) {
                             info("handle capture.")
                             ex.printKetherErrorMessage()
                             sender.sendMessage(" §5§l‹ ›§r §cException: §f${ex.localizedMessage}")
                             return@handle
                         }
                         sender.sendMessage(" §5§l‹ ›§r §aResult: §f$result")
                     }
                 } catch (ex: Throwable) {
                     info("try-catch capture.")
                     ex.printKetherErrorMessage()
                     sender.sendMessage(" §5§l‹ ›§r §cException: §f${ex.localizedMessage}")
                 }
             }
        }
    }

}