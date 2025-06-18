package top.lanscarlos.vulpecula.module.bacikal.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry
import top.lanscarlos.vulpecula.module.bacikal.BacikalService
import top.lanscarlos.vulpecula.module.bacikal.info
import top.lanscarlos.vulpecula.module.bacikal.quest.BacikalQuestExecutor

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.command
 *
 * @author Lanscarlos
 * @since 2024-11-22 17:17
 */
object ActionCommand {

    @CommandBody
    val action = subCommand {
        literal("registry", literal = structure)
        literal("timing", literal = timing)
    }

    val structure: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { BacikalRegistry.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                val parser = BacikalRegistry.get(id)
                val component = parser.buildStructure()
                component.sendTo(sender)
            }
        }
    }

    val timing: CommandComponent.() -> Unit = {
        dynamic("option") {
            restrict<ProxyCommandSender> { _, _, input ->
                input.matches("(\\d+)(?:x(\\d+))?".toRegex())
            }
        }.dynamic("content") {
            execute<ProxyCommandSender> { sender, context, content ->
                val option = "(\\d+)(?:x(\\d+))?".toRegex().matchEntire(context["option"])!!.groupValues
                val repeat = option[1].toInt()
                val group = option[2].ifBlank { "1" }.toInt()
                var time = top.lanscarlos.vulpecula.common.core.utils.timing()
                val quest = BacikalService.compile(content, "timing", emptyList())
                val compileTime = top.lanscarlos.vulpecula.common.core.utils.timing(time)
                time = top.lanscarlos.vulpecula.common.core.utils.timing()
                val completeTimes = List(group) {
                    repeat(repeat) {
                        BacikalQuestExecutor.execute(quest, "main", -1L, sender, emptyMap()).join()
                    }
                    top.lanscarlos.vulpecula.common.core.utils.timing(time)
                }
                val averageCompleteTime = completeTimes.average()

                // 分析
                sender.info { asLang("module-bacikal-command-timing-repeat", repeat) }
                sender.info { asLang("module-bacikal-command-timing-group", group) }
                sender.info { asLang("module-bacikal-command-timing-compile", compileTime) }
                sender.info { asLang("module-bacikal-command-timing-execute", averageCompleteTime) }
            }
        }
    }

}