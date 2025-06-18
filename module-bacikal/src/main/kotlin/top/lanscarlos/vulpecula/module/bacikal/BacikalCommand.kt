package top.lanscarlos.vulpecula.module.bacikal

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import taboolib.module.kether.printKetherErrorMessage
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalCompileException
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.bacikal.quest.BacikalQuestExecutor

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal
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
                     val quest = BacikalService.compile(content, "eval", emptyList())
                     BacikalQuestExecutor.execute(quest, "main", -1L, sender, emptyMap())
                         .handle { result, e ->
                             if (e != null) {
                                 val ex = e.cause as BacikalRuntimeException
                                 ex.printLocalizedMessage(sender, asLang("module-bacikal-service-name"))
                             } else {
                                 sender.sendMessage(" §5§l‹ ›§r §aResult: §f$result")
                             }
                         }
                 } catch (ex: BacikalCompileException) {
                     ex.printLocalizedMessage(sender, asLang("module-bacikal-service-name"))
                 } catch (ex: Throwable) {
                     sender.sendMessage(" §5§l‹ ›§r §cException: §f${ex.localizedMessage}")
                     ex.printKetherErrorMessage(true)
                 }
             }
        }
    }

}