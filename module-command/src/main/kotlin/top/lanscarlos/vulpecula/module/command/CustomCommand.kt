package top.lanscarlos.vulpecula.module.command

import top.lanscarlos.vulpecula.common.config.boolean
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.config.string
import top.lanscarlos.vulpecula.common.config.stringList
import top.lanscarlos.vulpecula.common.config.stringOrNull
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.function.registerCommand
import taboolib.common.platform.function.unregisterCommand
import taboolib.common.platform.function.warning
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.exception.DefaultLocalizedException
import top.lanscarlos.vulpecula.common.exception.InvalidTypeException
import top.lanscarlos.vulpecula.common.utils.asLang
import java.awt.Component
import java.util.HashSet
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 9:48
 */
class CustomCommand(val id: String, val config: Configuration) {

    val name: String by config.read("name").string()

    val aliases: List<String> by config.read("aliases").stringList(emptyList())

    val description: String by config.read("description").string("")

    val usage: String by config.read("usage").string("")

    val permission: String by config.read("permission").string("")

    val permissionMessage: String by config.read("permission-message").string("")

    val permissionDefault: PermissionDefault by config.read("permission-default").stringOrNull().convert(::parsePermissionDefault)

    val newParser: Boolean by config.read("new-parser").boolean(false)

    val mainNode: MainNode by config.read("main").convert(::parseMainNode)

    val root: CommandBase by config.read("components").convert(::parseCommandBase)

    /**
     * 注册命令
     * */
    fun register() {
        registerCommand(
            // 创建命令结构
            command = CommandStructure(
                name,
                aliases,
                description,
                usage,
                permission,
                permissionMessage,
                permissionDefault,
                permissionChildren = emptyMap(),
                newParser = newParser,
            ),
            // 创建执行器
            executor = object : CommandExecutor {
                override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): Boolean {
                    return root.execute(CommandContext(sender, command, name, root, newParser, args))
                }
            },
            // 创建补全器
            completer = object : CommandCompleter {
                override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): List<String>? {
                    return root.suggest(CommandContext(sender, command, name, root, newParser, args))
                }
            },
            // 传入原始命令构建器
            commandBuilder = {}
        )
    }

    /**
     * 注销命令
     * */
    fun unregister() {
        unregisterCommand(name)
        aliases.forEach { unregisterCommand(it) }
    }

    fun rebuild() {
        unregister()
//        root = buildNode().build()
        register()
    }

    fun parseMainNode(value: Any?): MainNode {
        require(value == null || value is ConfigurationSection) {
            throw InvalidTypeException(value ?: "null")
        }
        return MainNode(value ?: Configuration.empty())
    }

    fun parseCommandBase(components: Any?): CommandBase {
        if (components == null) {
            return mainNode.build()
        }
        require(components is ConfigurationSection) {
            throw InvalidTypeException(components)
        }

        // 整合父子关系
        val relation = HashMap<String, HashSet<String>>()
        for (key in components.getKeys(false)) {
            val parent = components.getString("$key.parent")
            require(!parent.isNullOrBlank()) {
                throw NodeUndefinedException(key)
                asLang("module-command-exception-field-not-found", key, "parent")
            }
            relation.computeIfAbsent(parent) { HashSet() } += key
        }

        // 深度搜索遍历创建节点
        val nodes = HashMap<String, Node>()
        val stack = LinkedList<String>()
        val visited = mutableSetOf<String>()

        nodes[mainNode.id] = mainNode
        stack += relation["main"] ?: emptyList()
        if (stack.isEmpty()) {
            warning("Node \"${mainNode.id}\" has no children.")
            return mainNode.build()
        }
        while (stack.isNotEmpty()) {
            val id = stack.pop()
            if (!visited.add(id)) {
                // 重复处理节点
                error(asLang("module-command-exception-key-conflict", id))
            }
            val section = components.getConfigurationSection(id)!!
            val parent = nodes[section.getString("parent")!!]
            require(parent != null) {
                asLang("module-command-exception-parent-not-found", id)
            }
            val node = LiteralNode(id, parent, section)
            parent.children += node
            nodes[id] = node
            visited += id

            // 载入子节点
            stack += relation[id] ?: continue
        }
        return mainNode.build()
    }

    private fun parsePermissionDefault(value: String?): PermissionDefault {
        if (value == null) {
            return PermissionDefault.OP
        }
        return PermissionDefault.entries.find { it.name.equals(value, true) }
            ?: error(asLang("module-command-exception-invalid-permission-default", id, value))
    }

    inner class IllegalPermissionException(value: String) : DefaultLocalizedException()

    inner class NodeUndefinedException(nodeId: String) : DefaultLocalizedException()

}