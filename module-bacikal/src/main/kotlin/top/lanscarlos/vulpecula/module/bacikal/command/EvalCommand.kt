package top.lanscarlos.vulpecula.module.bacikal.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import taboolib.module.kether.printKetherErrorMessage
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.bacikal.BacikalService
import top.lanscarlos.vulpecula.module.bacikal.exception.QuestCompileException
import top.lanscarlos.vulpecula.module.bacikal.exception.QuestRuntimeException
import top.lanscarlos.vulpecula.module.bacikal.quest.BacikalQuestExecutor

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.command
 *
 * @author Lanscarlos
 * @since 2025/6/18 11:11
 */
object EvalCommand {

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
                        val ex = e.cause as QuestRuntimeException
                        ex.notice(sender)
                    } else {
                        Lang.BACIKAL_COMMAND_EVAL_SUCCESS.info(sender, result ?: "null")
                    }
                }
        } catch (ex: QuestCompileException) {
            ex.notice(sender)
        } catch (ex: Throwable) {
            ex.printKetherErrorMessage(true)
            Lang.BACIKAL_COMMAND_EVAL_FAILURE.error(sender, ex.localizedMessage)
        }
    }

}