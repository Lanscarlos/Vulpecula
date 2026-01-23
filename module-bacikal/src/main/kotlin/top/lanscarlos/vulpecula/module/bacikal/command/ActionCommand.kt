package top.lanscarlos.vulpecula.module.bacikal.command

import org.bukkit.Bukkit
import taboolib.common.io.taboolibId
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.restrictInt
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.library.kether.QuestActionParser
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.kether.Kether
import taboolib.module.kether.RemoteActionParser
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.common.utils.TimeUtil
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry
import top.lanscarlos.vulpecula.module.bacikal.BacikalService
import top.lanscarlos.vulpecula.module.bacikal.error
import top.lanscarlos.vulpecula.module.bacikal.extension.NativeExtension
import top.lanscarlos.vulpecula.module.bacikal.info
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalActionParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ExceptionalActionParser
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
        literal("reload", literal = reload)
        literal("structure", literal = structure)
        literal("timing", literal = timing)
    }

    private val registry: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            displayBacikalActions(sender, false)
        }

        literal("detail") {
            execute<ProxyCommandSender> { sender, _, _ ->
                displayBacikalActions(sender, true)
            }
        }
        literal("remote") {
            execute<ProxyCommandSender> { sender, _, _ ->
                displayRemoteActions(sender)
            }
        }
        literal("local") {
            execute<ProxyCommandSender> { sender, _, _ ->
                displayLocalActions(sender)
            }
        }
    }

    private val reload: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            for (source in BacikalRegistry.getExtensionValues()) {
                try {
                    source.reload()
                    Lang.BACIKAL_COMMAND_RELOAD_SUCCESS.info(sender, source.name)
                } catch (e: Exception) {
                    Lang.BACIKAL_COMMAND_RELOAD_FAILURE.info(sender, source.name, e.localizedMessage)
                }
            }
        }

        dynamic("source") {
            suggest { BacikalRegistry.getExtensionKeys().toList() }
            execute<ProxyCommandSender> { sender, _, name ->
                try {
                    BacikalRegistry.getExtension(name).reload()
                    Lang.BACIKAL_COMMAND_RELOAD_SUCCESS.info(sender, name)
                } catch (e: Exception) {
                    Lang.BACIKAL_COMMAND_RELOAD_FAILURE.info(sender, name, e.localizedMessage)
                }
            }
        }
    }

    private val structure: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggest { BacikalRegistry.getActionParserKeys().toList() }
            execute<ProxyCommandSender> { sender, _, id ->
                val parser = BacikalRegistry.getActionParser(id)
                val component = parser.buildStructure(-1)
                component.sendTo(sender)
            }
        }.dynamic("depth") {
            restrictInt()
            execute<ProxyCommandSender> { sender, context, depth ->
                val id = context["id"]
                val parser = BacikalRegistry.getActionParser(id)
                val component = parser.buildStructure(depth.toInt())
                component.sendTo(sender)
            }
        }
    }

    private val timing: CommandComponent.() -> Unit = {
        dynamic("option") {
            restrict<ProxyCommandSender> { _, _, input ->
                input.matches("(\\d+)(?:x(\\d+))?".toRegex())
            }
        }.dynamic("content") {
            execute<ProxyCommandSender> { sender, context, content ->
                val option = "(\\d+)(?:x(\\d+))?".toRegex().matchEntire(context["option"])!!.groupValues
                val repeat = option[1].toInt()
                val group = option[2].ifBlank { "1" }.toInt()
                var time = TimeUtil.startTiming()
                val quest = BacikalService.compile(content, "timing", emptyList())
                val compileTime = TimeUtil.stopTiming(time)
                time = TimeUtil.startTiming()
                val completeTimes = List(group) {
                    repeat(repeat) {
                        BacikalQuestExecutor.execute(quest, "main", -1L, sender, emptyMap()).join()
                    }
                    TimeUtil.stopTiming(time)
                }
                val averageCompleteTime = completeTimes.average()

                // 分析
                Lang.BACIKAL_COMMAND_TIMING_REPEAT.info(sender, repeat)
                Lang.BACIKAL_COMMAND_TIMING_GROUP.info(sender, group)
                Lang.BACIKAL_COMMAND_TIMING_COMPILE.info(sender, compileTime)
                Lang.BACIKAL_COMMAND_TIMING_EXECUTE.info(sender, averageCompleteTime)
            }
        }
    }

    private fun displayBacikalActions(sender: ProxyCommandSender, detail: Boolean) {
        val bacikalParsers = BacikalRegistry.getActionParserValues()
            .filter { it !is ExceptionalActionParser && (detail || !it.id.contains('.')) }
        Lang.BACIKAL_COMMAND_REGISTRY_BACIKAL_ACTION.info(sender, bacikalParsers.size)
        for ((source, parsers) in bacikalParsers.groupBy { it.extension }) {
            val color = if (source is NativeExtension) "§3" else "§b"
            Lang.BACIKAL_COMMAND_REGISTRY_ITEM.info(
                sender,
                source.name,
                source.version,
                parsers.joinToString("§7, ") { "$color${it.id}" }
            )
        }

        // 显示注册异常的信息
        for (parser in BacikalRegistry.getExceptionalParsers()) {
            Lang.BACIKAL_COMMAND_REGISTRY_EXCEPTIONAL.error(sender, parser.exception.localizedMessage)
        }
    }

    private fun displayRemoteActions(sender: ProxyCommandSender) {
        val remoteParsers = Kether.scriptRegistry.getProperty<Map<String, Map<String, QuestActionParser>>>("parsers")!!
            .flatMap { it.value.values }
            .filterIsInstance<RemoteActionParser>()
        if (remoteParsers.isEmpty()) {
            Lang.BACIKAL_COMMAND_REGISTRY_REMOTE_EMPTY.info(sender)
        } else {
            Lang.BACIKAL_COMMAND_REGISTRY_REMOTE.info(sender, remoteParsers.size)
        }
        for ((pluginId, parsers) in remoteParsers.groupBy { it.remote.name }) {
            val plugin = Bukkit.getPluginManager().getPlugin(pluginId) ?: error("Unknown plugin id: $pluginId")
            val version = plugin.description.version
            Lang.BACIKAL_COMMAND_REGISTRY_ITEM.info(
                sender,
                pluginId,
                version,
                parsers.joinToString("§7, ") { "§a${it.action}" }
            )
        }
    }

    private fun displayLocalActions(sender: ProxyCommandSender) {
        val parsers = Kether.scriptRegistry.getProperty<Map<String, Map<String, QuestActionParser>>>("parsers")!!
            .flatMap { it.value.entries }.filter { it.value !is BacikalActionParser && it.value !is RemoteActionParser }
        Lang.BACIKAL_COMMAND_REGISTRY_LOCAL.info(sender, parsers.size)
        Lang.BACIKAL_COMMAND_REGISTRY_ITEM.info(
            sender,
            taboolibId,
            "6",
            parsers.joinToString("§7, ") { "§c${it.key}" }
        )
    }

}