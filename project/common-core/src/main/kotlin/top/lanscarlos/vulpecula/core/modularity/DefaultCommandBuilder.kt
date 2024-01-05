package top.lanscarlos.vulpecula.core.modularity

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.common.platform.command.component.CommandComponentLiteral
import taboolib.common.platform.command.restrictBoolean
import taboolib.common.platform.command.restrictDouble
import taboolib.common.platform.command.restrictInt
import taboolib.common.platform.command.suggestPlayers
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common5.cbool
import taboolib.expansion.createHelper
import taboolib.library.configuration.ConfigurationSection

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.modularity
 *
 * @author Lanscarlos
 * @since 2024-01-05 12:38
 */
class DefaultCommandBuilder(val context: DefaultCommand, val id: String, val section: ConfigurationSection) {

    val children = mutableSetOf<DefaultCommandBuilder>()

    /**
     * 构建指令
     * */
    fun build(index: Int): CommandComponent {
        val component = when {
            id == "main" -> CommandBase()
            "literal" in section || "aliases" in section -> buildLiteral(index)
            "dynamic" in section || "suggest" in section || "optional" in section -> buildDynamic(index)
            else -> buildLiteral(index)
        }

        // 构建执行器
        buildExecutor(component)

        // 加载子节点
        if (children.isNotEmpty()) {
            for (child in children) {
                component.children += child.build(index + 1)
            }
        }

        return component
    }

    private fun buildDynamic(index: Int): CommandComponent {
        val component = CommandComponentDynamic(
            section.getString("dynamic") ?: id,
            index,
            section.getBoolean("optional", false),
            section.getString("permission") ?: ""
        )

        section["suggest"]?.let { suggest ->
            buildSuggest(component, suggest, section.getBoolean("uncheck", false))
        }

        return component
    }

    private fun buildLiteral(index: Int): CommandComponent {
        val literal = mutableListOf(section.getString("name") ?: id)
        when (val aliases = section["aliases"]) {
            is String -> literal += aliases
            is Collection<*> -> literal += aliases.filterIsInstance<String>()
            is Array<*> -> literal += aliases.filterIsInstance<String>()
        }

        return CommandComponentLiteral(
            literal.toTypedArray(),
            section.getBoolean("hidden", false),
            index,
            optional = section.getBoolean("optional", false),
            permission = section.getString("permission") ?: ""
        )
    }

    private fun buildExecutor(component: CommandComponent) {
        if (section["execute"] == null) {
            if (id == "main") {
                // 若为主命令，则创建帮助执行器
                component.createHelper()
            }
            return
        }

        // 当前节点是否只允许玩家执行
        val playerOnly = section.getBoolean("player-only", false)

        // 获取参数节点列表
        val parameters = section.getMapList("args").mapIndexedNotNull { i, arg ->
            if (arg.size == 1) {
                val entry = arg.entries.first()
                CommandComponentDynamic(entry.key!!.toString(), component.index + i + 1, false, "").also {
                    buildSuggest(it, entry.value!!, false)
                }
            } else {
                val entry = arg.entries.firstOrNull {
                    it.key != "permission" && it.key != "uncheck" && it.key != "suggest" && it.key != "optional"
                }
                val name = arg["name"]?.toString() ?: entry?.key?.toString() ?: return@mapIndexedNotNull null
                val optional = arg["optional"]?.cbool ?: false
                val permission = arg["permission"]?.toString() ?: ""
                val uncheck = arg["uncheck"]?.cbool ?: false
                val suggest = arg["suggest"] ?: entry?.value ?: "*"

                CommandComponentDynamic(name, component.index + i + 1, optional, permission).also {
                    buildSuggest(it, suggest, uncheck)
                }
            }
        }

        // 若参数为空或第一个参数节点为可选，则为当前节点添加执行器
        if (parameters.isEmpty() || parameters.first().optional) {
            if (playerOnly) {
                executeQuest<Player>(component)
            } else {
                executeQuest<CommandSender>(component)
            }
        } else {
            // 为参数节点添加执行器
            val comments = parameters.map { it.comment }
            for ((i, parameter) in parameters.withIndex()) {

                // 连接参数节点
                if (i == 0) {
                    component.children += parameter
                } else {
                    parameters[i - 1].children += parameter
                }

                // 最后一个节点 或 后面的节点为可选节点时，为当前节点添加执行器
                if (i == parameters.lastIndex || parameters[i + 1].optional) {
                    if (playerOnly) {
                        executeQuest<Player>(parameter, component.index, comments)
                    } else {
                        executeQuest<CommandSender>(parameter, component.index, comments)
                    }
                }
            }
        }
    }

    private fun buildSuggest(component: CommandComponentDynamic, suggest: Any, uncheck: Boolean = false) {
        when (suggest) {
            "*" -> return
            "bool", "boolean" -> component.restrictBoolean()
            "int" -> component.restrictInt()
            "decimal", "double" -> component.restrictDouble()
            "player", "online" -> component.suggestPlayers()
            "offline" -> {
                component.suggestion<CommandSender>(uncheck = uncheck) { _, _ ->
                    Bukkit.getOfflinePlayers().mapNotNull { if (!it.isOnline) it.name else null }
                }
            }
            "players" -> {
                component.suggestion<CommandSender>(uncheck = uncheck) { _, _ ->
                    Bukkit.getOfflinePlayers().mapNotNull { it.name }
                }
            }
            "world" -> {
                component.suggestion<CommandSender>(uncheck = uncheck) { _, _ ->
                    Bukkit.getWorlds().mapNotNull { it.name }
                }
            }
            is Collection<*> -> {
                component.suggestion<CommandSender>(uncheck) { _, _ ->
                    suggest.mapNotNull { it?.toString() }
                }
            }
            is Array<*> -> {
                component.suggestion<CommandSender>(uncheck) { _, _ ->
                    suggest.mapNotNull { it?.toString() }
                }
            }
        }
    }

    private inline fun <reified T: Any> executeQuest(component: CommandComponent, index: Int = component.index, comments: List<String> = emptyList()) {
        val quest = context.quest

        component.execute<T> { sender, context, argument ->
            quest.runActions("@$id") {
                this.sender = adaptCommandSender(sender)
                setVariable("@Context", context)
                setVariables("@Arg", "argument", "arg", value = argument)
                if (sender is Player) {
                    setVariable("@Player", sender)
                    setVariable("player", sender)
                }

                // 防止根命令获取空参数
                try {
                    val cache = context.args()
                    for ((i, arg) in cache.withIndex()) {
                        setVariable("arg$i", arg)
                        if (i > index && i <= comments.size + index) {
                            setVariable(comments[i - index - 1], value = arg)
                        }
                    }
                    setVariables("@Args", "args", value = cache)
                } catch (ignored: Exception) {
                    setVariables("@Args", "args", value = emptyArray<String>())
                }
            }
        }
    }

}