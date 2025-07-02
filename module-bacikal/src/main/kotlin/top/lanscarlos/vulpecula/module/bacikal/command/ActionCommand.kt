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
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry
import top.lanscarlos.vulpecula.module.bacikal.BacikalService
import top.lanscarlos.vulpecula.module.bacikal.action.BuiltInActionSource
import top.lanscarlos.vulpecula.module.bacikal.error
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

    val registry: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            val bacikalCount = BacikalRegistry.values()
                .filter { it !is ExceptionalActionParser && !it.id.contains('.') }
                .size
            val remoteCount = Kether.scriptRegistry.getProperty<Map<String, Map<String, QuestActionParser>>>("parsers")!!
                .flatMap { it.value.values }
                .filterIsInstance<RemoteActionParser>()
                .size
            sender.info {
                asLang("module-bacikal-command-registry-display-all", bacikalCount + remoteCount)
            }
            displayBacikalActions(sender, false)
            displayRemoteActions(sender, false)
        }

        literal("bacikal") {
            execute<ProxyCommandSender> { sender, _, _ ->
                displayBacikalActions(sender, true)
            }
        }
        literal("remote") {
            execute<ProxyCommandSender> { sender, _, _ ->
                displayRemoteActions(sender, true)
            }
        }
        literal("local") {
            execute<ProxyCommandSender> { sender, _, _ ->
                displayLocalActions(sender)
            }
        }
    }

    val reload: CommandComponent.() -> Unit = {
        dynamic("source") {
            suggest { BacikalRegistry.sources.keys.toList() }
            execute<ProxyCommandSender> { sender, _, sourceName ->
                try {
                    BacikalRegistry.sources[sourceName]!!.reload()
                    sender.info { asLang("module-bacikal-command-reload-success", sourceName) }
                } catch (e: Exception) {
                    sender.error { asLang("module-bacikal-command-reload-failure", sourceName, e.localizedMessage) }
                }
            }
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

    private fun displayBacikalActions(sender: ProxyCommandSender, header: Boolean) {
        val bacikalParsers = BacikalRegistry.values().filter { it !is ExceptionalActionParser && !it.id.contains('.') }
        if (header) {
            sender.info { asLang("module-bacikal-command-registry-display-bacikal", bacikalParsers.size) }
        }
        for ((source, parsers) in bacikalParsers.groupBy { it.source }) {
            val color = if (source is BuiltInActionSource) "§3" else "§b"
            sender.info {
                asLang(
                    "module-bacikal-command-registry-display-item",
                    source.name,
                    source.version,
                    parsers.joinToString("§7, ") { "$color${it.id}" }
                )
            }
        }

        // 显示注册异常的信息
        for (parser in BacikalRegistry.getExceptionalParsers()) {
            sender.error { parser.exception.localizedMessage }
        }
    }

    private fun displayRemoteActions(sender: ProxyCommandSender, header: Boolean) {
        val remoteParsers = Kether.scriptRegistry.getProperty<Map<String, Map<String, QuestActionParser>>>("parsers")!!
            .flatMap { it.value.values }
            .filterIsInstance<RemoteActionParser>()
        if (header) {
            sender.info {
                asLang(
                    "module-bacikal-command-registry-display-remote${if (remoteParsers.isEmpty()) "-empty" else ""}",
                    remoteParsers.size
                )
            }
        }
        for ((pluginId, parsers) in remoteParsers.groupBy { it.remote.name }) {
            val plugin = Bukkit.getPluginManager().getPlugin(pluginId) ?: error("Unknown plugin id: $pluginId")
            val version = plugin.description.version
            sender.info {
                asLang(
                    "module-bacikal-command-registry-display-item",
                    pluginId,
                    version,
                    parsers.joinToString("§7, ") { "§a${it.action}" }
                )
            }
        }
    }

    private fun displayLocalActions(sender: ProxyCommandSender) {
        val parsers = Kether.scriptRegistry.getProperty<Map<String, Map<String, QuestActionParser>>>("parsers")!!
            .flatMap { it.value.entries }.filter { it.value !is BacikalActionParser && it.value !is RemoteActionParser }
        sender.info { asLang("module-bacikal-command-registry-display-local", parsers.size) }
        sender.info {
            asLang(
                "module-bacikal-command-registry-display-item",
                taboolibId,
                "6",
                parsers.joinToString("§7, ") { "§c${it.key}" }
            )
        }
    }

}