package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.function.warning
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.*
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

    val node: Node = buildNode()

    fun rebuild() {}

    fun buildNode(): Node {
        // 创建主节点
        val main = MainNode(config.getConfigurationSection("main") ?: Configuration.empty())
        val components = config.getConfigurationSection("components") ?: return main

        // 整合父子关系
        val relation = HashMap<String, HashSet<String>>()
        for (key in components.getKeys(false)) {
            val parent = components.getString("$key.parent")
            require(parent != null) { "Parent field is required for component \"$key\"." }
            relation.computeIfAbsent(parent) { HashSet() } += key
        }

        // 深度搜索遍历创建节点
        val nodes = HashMap<String, Node>()
        val stack = LinkedList<String>()
        val visited = mutableSetOf<String>()

        nodes[main.id] = main
        stack += relation["main"] ?: emptyList()
        if (stack.isEmpty()) {
            warning("Node \"${main.id}\" has no children.")
            return main
        }
        while (stack.isNotEmpty()) {
            val id = stack.pop()
            if (!visited.add(id)) {
                // 重复处理节点
                error("Visited duplicate node $id.")
            }
            val section = components.getConfigurationSection(id)!!
            val parent = nodes[section.getString("parent")!!]
            require(parent != null) { "Parent not found for component \"$id\"." }
            val node = when {
                "literal" in section || "aliases" in section -> LiteralNode(id, parent, section)
                "dynamic" in section || "suggest" in section || "optional" in section -> DynamicNode(id, parent, section)
                else -> LiteralNode(id, parent, section)
            }
            parent.children += node
            nodes[id] = node
            visited += id

            // 载入子节点
            stack += relation[id] ?: continue
        }

        return main
    }

    private fun parsePermissionDefault(value: String?): PermissionDefault {
        if (value == null) {
            return PermissionDefault.OP
        }
        return PermissionDefault.entries.find { it.name.equals(value, true) } ?: error("Unknown permission default: $value")
    }

}