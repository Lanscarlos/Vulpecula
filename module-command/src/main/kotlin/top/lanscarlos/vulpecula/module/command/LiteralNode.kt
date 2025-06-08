package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentLiteral
import taboolib.common.platform.function.warning
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.applicative.applicativeBoolean
import top.lanscarlos.vulpecula.common.applicative.applicativeStringList
import top.lanscarlos.vulpecula.common.core.utils.asLang
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

    val parameters: List<DynamicNode> = parseParameters(section.getMapList("parameters"))

    override fun build(): CommandComponent {
        val component = CommandComponentLiteral(
            aliases = arrayOf(name, *aliases.toTypedArray()),
            hidden = hidden,
            index = index,
            optional = optional,
            permission = permission
        )

        // 执行器
        component.execute(bind = ProxyCommandSender::class.java, function = ::execute)

        // 处理子节点
        for (child in children) {
            component.children += child.build()
        }

        return component
    }

    override fun execute(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String) {
        if (parameters.isNotEmpty() && !parameters.first().optional) {
            warning("LiteralNode 缺失必要参数: ${parameters.first().name}")
            sender.error(sync = true) { asLang("module-command-exception-missing-argument", parameters.first().name) }
            return
        }
        super.execute(sender, context, argument)
    }

    private fun parseParameters(value: List<Map<*, *>>): List<DynamicNode> {
        if (value.isEmpty()) {
            return emptyList()
        }
        require(executor != null) {
            asLang("module-command-exception-executor-not-found", id)
        }
        val list = LinkedList<DynamicNode>()
        var parent: Node = this
        for (section in value) {
            val id = section["name"]!!.toString()
            val node = ParameterNode(id, parent, section.plus("execute" to executor.script))
            list += node
            parent.children += node
            parent = node
        }
        return list
    }

    inner class ParameterNode(id: String, parent: Node?, section: Map<*, *>) : DynamicNode(id, parent, section) {

        override fun execute(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String) {
            if (children.isNotEmpty() && !children.single().optional) {
                warning("ParameterNode 缺失必要参数: ${children.single().name}")
                sender.error(sync = true) { asLang("module-command-exception-missing-argument", children.single().name) }
                return
            }
            super.execute(sender, context, argument)
        }

    }

}