package top.lanscarlos.vulpecula.internal

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.common.platform.command.component.CommandComponentLiteral
import taboolib.common.platform.command.suggest
import taboolib.common.platform.command.suggestPlayers
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.info
import taboolib.expansion.createHelper
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.kether.printKetherErrorMessage
import top.lanscarlos.vulpecula.utils.getStringOrList
import top.lanscarlos.vulpecula.utils.runActions
import top.lanscarlos.vulpecula.utils.setVariable
import top.lanscarlos.vulpecula.utils.toKetherScript

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2023-02-09 20:15
 */
class CommandComponentBuilder(val id: String, val section: ConfigurationSection, val legacy: Boolean) : ScriptCompiler {

    val children = mutableSetOf<CommandComponentBuilder>()

    init {
        if (legacy) {
            section.getConfigurationSection("dynamic")?.let {
                children += CommandComponentBuilder("dynamic", it, legacy)
            }

            section.getConfigurationSection("literal")?.let { next ->
                info("inner has literal...")
                for (literal in next.getKeys(false)) {
                    info("inner load literal $literal")
                    val node = next.getConfigurationSection(literal) ?: continue
                    info("inner load literal $literal x2")
                    children += CommandComponentBuilder(literal, node, legacy)
                }
            }
        }
    }

    fun build(index: Int): CommandComponent {
        val component = when {
            id == "main" -> CommandBase().also { it.createHelper() }
            legacy -> {
                if (id == "dynamic") {
                    buildDynamic(index)
                } else {
                    buildDLiteral(index)
                }
            }
            "literal" in section || "aliases" in section -> buildDLiteral(index)
            "dynamic" in section || "suggest" in section || "optional" in section -> buildDynamic(index)
            else -> buildDLiteral(index)
        }

        section["execute"]?.let {
            val showArgs = component !is CommandBase
            if (section.getBoolean("player-only", false)) {
                component.executeScript<Player>(it, showArgs)
            } else {
                component.executeScript<CommandSender>(it, showArgs)
            }
        }

        // 加载子节点
        if (children.isNotEmpty()) {
            for (child in children) {
                component.children += child.build(index + 1)
            }
        }

        return component
    }

    fun buildDynamic(index: Int) : CommandComponent {
        val component = CommandComponentDynamic(
            index,
            if (!legacy) section.getString("dynamic") ?: id else section.getString("comment") ?: "...",
            section.getBoolean("optional", false),
            section.getString("permission") ?: ""
        )

        section["suggest"]?.let { suggest ->
            val uncheck = section.getBoolean("uncheck", false)
            when (suggest) {
                "*" -> component.suggest { Bukkit.getOfflinePlayers().mapNotNull { it.name } }
                "@Players", "@players" -> component.suggestPlayers()
                is Collection<*> -> component.suggestLiteral(suggest, uncheck)
                else -> {
                    val force = section.getBoolean("suggest-force", false)

                    if (section.getBoolean("player-only", false)) {
                        component.suggestScript<Player>(suggest, uncheck, force)
                    } else {
                        component.suggestScript<CommandSender>(suggest, uncheck, force)
                    }
                }
            }
        }

        return component
    }

    fun buildDLiteral(index: Int): CommandComponent {

        val literal = if (legacy) {
            listOf(id)
        } else if (section.contains("literal")) {
            section.getStringOrList("literal")
        } else {
            section.getStringOrList("aliases").plus(id)
        }

        return CommandComponentLiteral(
            index,
            *literal.toTypedArray(),
            optional = section.getBoolean("optional", false),
            permission = section.getString("permission") ?: ""
        )
    }

    /**
     * 根据所给的文本进行命令提示
     * */
    fun CommandComponentDynamic.suggestLiteral(literal: Collection<*>, uncheck: Boolean) {
        this.suggestion<ProxyCommandSender>(uncheck) { _, _ -> literal.mapNotNull { it?.toString() } }
    }

    /**
     * @param force 是否强制等待脚本返回结果
     * */
    inline fun <reified T: Any> CommandComponentDynamic.suggestScript(source: Any, uncheck: Boolean, force: Boolean) {
        val builder = StringBuilder()
        val content = buildSection(source, builder).extract()
        builder.append("def main = {\n")
        builder.appendWithIndent(content, suffix = "\n")
        builder.append("}")

        val script = try {
            builder.toString().toKetherScript()
        } catch (e: Exception) {
            e.printKetherErrorMessage()
            return
        }

        this.suggestion<T>(uncheck) { sender, context ->
            val future = script.runActions {
                this.sender = adaptCommandSender(sender)
                setVariable("@Context", "context", value = context)
                setVariable("@Args", "args", value = context.args())
                if (sender is Player) {
                    setVariable("@Player", "player", value = sender)
                }
            }

            val result = if (force) {
                future.join()
            } else {
                future.getNow(null)
            }

            when (result) {
                is Array<*> -> result.mapNotNull { it?.toString() }
                is Collection<*> -> result.mapNotNull { it?.toString() }
                else -> if (result != null) listOf(result.toString()) else emptyList()
            }
        }
    }

    inline fun <reified T: Any> CommandComponent.executeScript(source: Any, showArgs: Boolean) {
        val builder = StringBuilder()
        val content = buildSection(source, builder).extract()
        builder.append("def main = {\n")
        builder.appendWithIndent(content, suffix = "\n")
        builder.append("}")

        val script = try {
            builder.toString().toKetherScript()
        } catch (e: Exception) {
            e.printKetherErrorMessage()
            return
        }

        this.execute<T> { sender, context, argument ->
            script.runActions {
                this.sender = adaptCommandSender(sender)
                setVariable("@Context", "context", value = context)
                setVariable("@Arg", "arg", value = argument)

                // 防止根命令获取空参数
                if (showArgs) {
                    val args = context.args()
                    setVariable("@Args", "args", value = args)
                    for ((i, arg) in args.withIndex()) {
                        set("arg$i", arg)
                    }
                } else {
                    setVariable("@Args", "args", value = emptyArray<String>())
                }

                if (sender is Player) {
                    setVariable("@Player", "player", value = sender)
                }
            }
        }
    }

    override fun buildSource(): StringBuilder {
        return StringBuilder()
    }

    override fun compileScript() {
    }
}