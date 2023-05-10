package top.lanscarlos.vulpecula.internal

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.common.platform.command.component.CommandComponentLiteral
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common5.cbool
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.bacikal.script.BacikalScript
import top.lanscarlos.vulpecula.bacikal.buildBacikalScript
import top.lanscarlos.vulpecula.utils.*
import top.lanscarlos.vulpecula.utils.Debug.debug

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2023-02-09 20:15
 */
class CommandComponentBuilder(val id: String, val section: ConfigurationSection) {

    val children = mutableSetOf<CommandComponentBuilder>()

    fun build(index: Int): CommandComponent {
        val component = when {
            "literal" in section || "aliases" in section -> buildLiteral(index)
            "dynamic" in section || "suggest" in section || "optional" in section -> buildDynamic(index)
            else -> buildLiteral(index)
        }

        // 构建执行器
        component.buildExecutor()

        // 加载子节点
        if (children.isNotEmpty()) {
            for (child in children) {
                component.children += child.build(index + 1)
            }
        }

        return component
    }

    fun buildDynamic(index: Int): CommandComponent {
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

        val literal = mutableListOf(section.getString("name") ?: id)
        literal += section.getStringOrList("aliases")

        return CommandComponentLiteral(
            index,
            *literal.toTypedArray(),
            optional = section.getBoolean("optional", false),
            permission = section.getString("permission") ?: ""
        )
    }

    fun CommandComponent.buildExecutor() {

        val executable = section["execute"] ?: return

        // 执行的脚本
        val script = buildBacikalScript {
            appendContent(executable)
        }

        debug(Debug.HIGHEST, "custom command \"$id\" build source:\n${script.source}")

        // 当前节点是否只允许玩家执行
        val playerOnly = section.getBoolean("player-only", false)

        // 获取参数节点列表
        val parameters = section.getMapList("args").mapIndexedNotNull { i, arg ->
            if (arg.size == 1) {
                val entry = arg.entries.first()
                CommandComponentDynamic(index + i + 1, entry.key!!.toString(), false, "").also {
                    it.buildSuggest(entry.value!!, false)
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

                CommandComponentDynamic(index + i + 1, name, optional, permission).also {
                    it.buildSuggest(suggest, uncheck)
                }
            }
        }

        val comments = parameters.map { it.comment }
        for ((i, parameter) in parameters.withIndex()) {

            // 连接参数节点
            if (i == 0) {
                this.children += parameter
            } else {
                parameters[i - 1].children += parameter
            }

            // 最后一个节点 或 后面的节点为可选节点时，为当前节点添加执行器
            if (i == parameters.lastIndex || parameters[i + 1].optional) {
                if (playerOnly) {
                    parameter.executeScript<Player>(script, index, comments)
                } else {
                    parameter.executeScript<CommandSender>(script, index, comments)
                }
            }
        }

        // 若参数为空或第一个参数节点为可选，则为当前节点添加执行器
        if (parameters.isEmpty() || parameters.first().optional) {
            if (playerOnly) {
                this.executeScript<Player>(script)
            } else {
                this.executeScript<CommandSender>(script)
            }
        }
    }

    /**
     * 构建命令提示
     * */
    fun CommandComponentDynamic.buildSuggest(suggest: Any, uncheck: Boolean = false) {
        when (suggest) {
            "*", "unsuggest" -> return
            "bool" -> {
                this.restrictBoolean()
            }
            "int" -> {
                this.restrictInt()
            }
            "decimal", "double" -> {
                this.restrictDouble()
            }
            "player", "online" -> {
                this.suggestion<ProxyCommandSender>(uncheck = uncheck) { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
            }
            "offline" -> {
                this.suggestion<ProxyCommandSender>(uncheck = uncheck) { _, _ ->
                    Bukkit.getOfflinePlayers().mapNotNull { if (!it.isOnline) it.name else null }
                }
            }
            "players" -> {
                this.suggestion<ProxyCommandSender>(uncheck = uncheck) { _, _ ->
                    Bukkit.getOfflinePlayers().mapNotNull { it.name }
                }
            }
            is Collection<*> -> {
                this.suggestion<ProxyCommandSender>(uncheck) { _, _ ->
                    suggest.mapNotNull { it?.toString() }
                }
            }
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
     * @param force 是否强制等待脚本返回结果
     * */
    inline fun <reified T : Any> CommandComponentDynamic.suggestScript(source: Any, uncheck: Boolean, force: Boolean) {
        val script = buildBacikalScript {
            appendContent(source)
        }

        this.suggestion<T>(uncheck) { sender, context ->
            val future = script.runActions {
                this.sender = adaptCommandSender(sender)
                set("@Context", context)
                setVariable("@Args", "args", value = context.args())
                if (sender is Player) {
                    set("@Player", sender)
                    set("player", sender)
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

    inline fun <reified T : Any> CommandComponent.executeScript(script: BacikalScript, index: Int = this.index, comments: List<String> = emptyList()) {

        this.execute<T> { sender, context, argument ->
            script.runActions {
                this.sender = adaptCommandSender(sender)
                set("@Context", context)
                setVariable("@Arg", "argument", "arg", value = argument)
                if (sender is Player) {
                    set("@Player", sender)
                    set("player", sender)
                }

                // 防止根命令获取空参数
                try {
                    val cache = context.args()
                    for ((i, arg) in cache.withIndex()) {
                        set("arg$i", arg)
                        if (i > index && i <= comments.size + index) {
                            set(comments[i - index - 1], value = arg)
                        }
                    }
                    setVariable("@Args", "args", value = cache)
                } catch (ignored: Exception) {
                    setVariable("@Args", "args", value = emptyArray<String>())
                }
            }
        }
    }
}