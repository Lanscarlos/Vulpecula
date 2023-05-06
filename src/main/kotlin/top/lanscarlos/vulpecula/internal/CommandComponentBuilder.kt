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
import taboolib.common5.cbool
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
class CommandComponentBuilder(val id: String, val section: ConfigurationSection) : ScriptCompiler {

    val children = mutableSetOf<CommandComponentBuilder>()

    fun build(index: Int): CommandComponent {
        val component = when {
            id == "main" -> CommandBase().also { it.createHelper() }
            "literal" in section || "aliases" in section -> buildLiteral(index)
            "dynamic" in section || "suggest" in section || "optional" in section -> buildDynamic(index)
            else -> buildLiteral(index)
        }

        section["execute"]?.let { executable ->

            // 当前节点是否只允许玩家执行
            val playerOnly = section.getBoolean("player-only", false)

            // 获取参数节点列表
            var optional = false
            val parameters = section.getMapList("args").mapIndexedNotNull { i, arg ->
                if (arg.size == 1) {
                    val entry = arg.entries.first()
                    CommandComponentDynamic(index + i, entry.key!!.toString(), optional, "").also {
                        it.buildSuggest(entry.value!!, false)
                    }
                } else {
                    val name = arg["name"]?.toString() ?: return@mapIndexedNotNull null
                    val permission = arg["permission"]?.toString() ?: ""
                    val uncheck = arg["uncheck"]?.cbool ?: false
                    val suggest = arg["suggest"] ?: "*"
                    optional = optional || arg["optional"]?.cbool ?: false

                    CommandComponentDynamic(index + i, name, optional, permission).also {
                        it.buildSuggest(suggest, uncheck)
                    }
                }
            }

            for ((i, parameter) in parameters.withIndex()) {

                // 连接所有参数节点
                if (i > 0) {
                    parameters[i - 1].children += parameter
                }

                // 当前节点可选或为最后一个节点
                if (parameter.optional || i == parameters.lastIndex) {
                    if (playerOnly) {
                        parameter.executeScript<Player>(executable, parameters.map { it.comment })
                    } else {
                        parameter.executeScript<CommandSender>(executable, parameters.map { it.comment })
                    }
                }
            }

            // 若参数节点不为空，连接首个参数节点
            if (parameters.isNotEmpty()) {
                component.children += parameters.first()

                // 若第一个参数节点为可选，则为当前节点添加执行器
                if (parameters.first().optional) {
                    if (playerOnly) {
                        component.executeScript<Player>(executable, emptyList())
                    } else {
                        component.executeScript<CommandSender>(executable, emptyList())
                    }
                }
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
            section.getString("dynamic") ?: id,
            section.getBoolean("optional", false),
            section.getString("permission") ?: ""
        )

        section["suggest"]?.let { suggest ->
            component.buildSuggest(suggest, section.getBoolean("uncheck", false))
        }

        return component
    }

    fun buildLiteral(index: Int): CommandComponent {

        val literal = if (section.contains("literal")) {
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
     * 构建命令提示
     * */
    fun CommandComponentDynamic.buildSuggest(suggest: Any, uncheck: Boolean = false) {
        when (suggest) {
            "*" -> return
            "@Players", "@players" -> this.suggestPlayers()
            is Collection<*> -> this.suggestLiteral(suggest, uncheck)
            else -> {
                val force = section.getBoolean("suggest-force", false)

                if (section.getBoolean("player-only", false)) {
                    this.suggestScript<Player>(suggest, uncheck, force)
                } else {
                    this.suggestScript<CommandSender>(suggest, uncheck, force)
                }
            }
        }
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

    inline fun <reified T: Any> CommandComponent.executeScript(source: Any, args: List<String>) {
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
                try {
                    val cache = context.args()
                    val offset = cache.size - args.size
                    for ((i, arg) in cache.withIndex()) {
                        set("arg$i", arg)
                        if (i >= offset) {
                            set(args[i - offset], value = arg)
                        }
                    }
                    setVariable("@Args", "args", value = cache)
                } catch (ignored: Exception) {
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