package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentLiteral
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.applicative.applicativeBoolean
import top.lanscarlos.vulpecula.common.applicative.applicativeStringList
import java.util.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 13:23
 */
class LiteralNode(id: String, parent: Node?, section: ConfigurationSection) : Node(id, parent, section) {

    val aliases: List<String> = section["aliases"].applicativeStringList(emptyList())

    val hidden: Boolean = section["hidden"].applicativeBoolean(false)

    init {
        parseParameters(section.getMapList("parameters"), section["execute"])
    }

    override fun build(): CommandComponent {
        val component = CommandComponentLiteral(
            aliases = arrayOf(name, *aliases.toTypedArray()),
            hidden = hidden,
            index = index,
            optional = optional,
            permission = permission
        )

        if (executor != null) {
            component.execute(bind = senderClass, function = executor::execute)
        }

        // 处理子节点
        for (child in children) {
            component.children += child.build()
        }

        return component
    }

    private fun parseParameters(value: List<Map<*, *>>, execution: Any?): List<DynamicNode> {
        val list = LinkedList<DynamicNode>()
        var parent: Node = this
        for (section in value) {
            val id = section["name"]!!.toString()
            val node = DynamicNode(id, parent, section.plus("execute" to execution))
            parent.children += node
            parent = node
        }
        return list
    }

}