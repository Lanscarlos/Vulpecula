package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentLiteral
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.boolean
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldReadException
import top.lanscarlos.vulpecula.common.config.mapList
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.config.stringList
import top.lanscarlos.vulpecula.common.exception.DefaultLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.common.utils.withConsole
import java.util.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 13:23
 */
open class LiteralNode(id: String, parent: Node?, config: ConfigurationSection) : Node(id, parent, config) {

    val aliases: List<String> by config.read("aliases").stringList(emptyList())

    val hidden: Boolean by config.read("hidden").boolean(false)

    val parameters: List<ParameterNode> by config.read("parameters").mapList().convert(::parseParameters)

    override fun build(): CommandComponent {
        val component = CommandComponentLiteral(
            aliases = arrayOf(name, *aliases.toTypedArray()),
            hidden = hidden,
            index = index,
            optional = optional,
            permission = permission
        )

        // 执行器
        if (playerRequired) {
            component.execute(bind = ProxyPlayer::class.java, function = ::execute)
        } else {
            component.execute(bind = ProxyCommandSender::class.java, function = ::execute)
        }

        // 处理子节点
        for (child in children) {
            component.children += child.build()
        }

        return component
    }

    override fun execute(sender: ProxyPlayer, context: CommandContext<ProxyPlayer>, argument: String) {
        if (parameters.isNotEmpty() && !parameters.first().optional) {
            Lang.MODULE_COMMAND_MISSING_ARGUMENT.error(sender.withConsole(), parameters.first().name)
            return
        }
        super.execute(sender, context, argument)
    }

    override fun execute(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String) {
        if (parameters.isNotEmpty() && !parameters.first().optional) {
            Lang.MODULE_COMMAND_MISSING_ARGUMENT.error(sender.withConsole(), parameters.first().name)
            return
        }
        super.execute(sender, context, argument)
    }

    private fun parseParameters(value: List<Map<*, *>>): List<ParameterNode> {
        if (value.isEmpty()) {
            return emptyList()
        }
        val executor = executor ?: throw ExecutorNotFoundException(id)
        val list = LinkedList<ParameterNode>()
        var parent: Node = this
        for ((index, section) in value.withIndex()) {
            val id = section["name"]?.toString() ?: throw ParameterNameUndefinedException(this.id, index)
            val config = Configuration.fromMap(mapOf("section" to section)).getConfigurationSection("section")!!
            val node = try {
                ParameterNode(id, parent, config, executor.script)
            } catch (e: ParameterNode.StrategyConflictException) {
                throw e.also { it.arguments[0] = this.id }
            } catch (e: ConfigFieldReadException) {
                when (val cause = e.cause) {
                    is ParameterNode.IllegalStrategyException -> {
                        throw cause.also { it.arguments[0] = this.id }
                    }
                    else -> throw e
                }
            }
            list += node
            parent.children += node
            parent = node
        }
        return list
    }

    class ExecutorNotFoundException(nodeId: String) :
        DefaultLocalizedException(Lang.MODULE_COMMAND_EXECUTOR_NOT_FOUND, arrayOf(nodeId))

    class ParameterNameUndefinedException(nodeId: String, index: Int) :
        DefaultLocalizedException(Lang.MODULE_COMMAND_PARAMETER_NAME_UNDEFINED, arrayOf(nodeId, index + 1))

}