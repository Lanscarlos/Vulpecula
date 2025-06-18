package top.lanscarlos.vulpecula.module.bacikal.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import taboolib.module.kether.printKetherErrorMessage
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.BacikalService
import top.lanscarlos.vulpecula.module.bacikal.error
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalCompileException
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.bacikal.info
import top.lanscarlos.vulpecula.module.bacikal.quest.BacikalQuestExecutor

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.command
 *
 * @author Lanscarlos
 * @since 2025/6/18 11:11
 */
object EvalCommand {

    val module: String get() = asLang("module-bacikal-service-name")

    @CommandBody
    val eval = subCommand {
        dynamic("content") {
            execute<ProxyCommandSender> { sender, _, content ->
                eval(sender, content)
            }
        }
    }

    private fun eval(sender: ProxyCommandSender, content: String) {
        try {
            val quest = BacikalService.compile(content, "eval", emptyList())
            BacikalQuestExecutor.execute(quest, "main", -1L, sender, emptyMap())
                .handle { result, e ->
                    if (e != null) {
                        val ex = e.cause as BacikalRuntimeException
                        ex.printLocalizedMessage(sender, module)
                    } else {
                        sender.info { asLang("module-bacikal-command-eval-success", result ?: "null") }
                    }
                }
        } catch (ex: BacikalCompileException) {
            ex.printLocalizedMessage(sender, module)
        } catch (ex: Throwable) {
            ex.printKetherErrorMessage(true)
            sender.error { asLang("module-bacikal-command-eval-failure", ex.localizedMessage) }
        }
    }

}