package top.lanscarlos.vulpecula.module.bacikal.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.restrictInt
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.module.chat.colored
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.common.diagram.TableDiagram
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
        literal("registry", literal = registry)
        literal("structure", literal = structure)
        literal("timing", literal = timing)
    }

    val registry: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            val diagram = TableDiagram()
            diagram.addHeader("语句")
            diagram.addHeader("命名空间")
            diagram.addHeader("状态")
            diagram.addHeader("版本")
            diagram.addHeader("来源")
            diagram.addTextRow(listOf("command", "vulpecula", "&c已禁用".colored(), "v3.0.0", "内置"))
            diagram.addTextRow(listOf("dispatcher", "vulpecula", "&a启用".colored(), "v3.0.0", "内置"))
            diagram.addTextRow(listOf("schedule", "vulpecula", "&a已启用".colored(), "v3.0.0", "内置"))
            diagram.addTextRow(listOf("script", "vulpecula", "&c已禁用".colored(), "v3.0.0", "内置"))
            diagram.addTextRow(listOf("event", "vulpecula", "&a已启用".colored(), "v1.0.0", "action-event.jar"))
            diagram.build().sendTo(sender)
        }
    }

    val structure: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { BacikalRegistry.keys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                val parser = BacikalRegistry.get(id)
                val component = parser.buildStructure(-1)
                component.sendTo(sender)
            }
        }.dynamic("depth") {
            restrictInt()
            execute<ProxyCommandSender> { sender, context, depth ->
                val id = context["id"]
                val parser = BacikalRegistry.get(id)
                val component = parser.buildStructure(depth.toInt())
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