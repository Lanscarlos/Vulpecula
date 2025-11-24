package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.function.warning
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.applicative.applicativeBoolean
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.module.script.Script
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 10:31
 */
abstract class Node(val id: String, val parent: Node?, section: Map<*, *>) {

    constructor(id: String, parent: Node?, section: ConfigurationSection) : this(id, parent, section.toMap())

    val name: String = section["name"]?.toString() ?: id

    val permission = section["permission"]?.toString() ?: ""

    val optional = section["optional"].applicativeBoolean(false)

    val playerRequired = section["require-player"].applicativeBoolean(false)

    val disableSuccessMessage = section["disable-success-message"].applicativeBoolean(false)

    val children = LinkedList<Node>()

    /**
     * 父子关系链条
     * */
    val chain: List<Node>

    /**
     * 索引
     * */
    val index: Int

    val executor: ScriptExecutor?

    init {
        chain = parseNodeChain()
        index = chain.size - 1
        executor = section["execute"]?.let(::parseExecution)
    }

    abstract fun build(): CommandComponent

    open fun execute(sender: ProxyPlayer, context: CommandContext<ProxyPlayer>, argument: String) {
        if (executor == null) {
            warning("此处无执行器.")
            return
        }
        executor.execute(sender, context)
    }

    open fun execute(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String) {
        if (executor == null) {
            warning("此处无执行器.")
            return
        }
        executor.execute(sender, context)
    }

    protected fun transformArgs(rawArgs: List<String>): Map<String, Any> {
        val args = mutableMapOf<String, Any>("args" to rawArgs)
        var index = 0
        for ((i, rawArg) in rawArgs.withIndex()) {
            val node = chain[i] as? ParameterNode ?: continue
            args["arg$index"] = rawArg
            args[node.name] = rawArg
            index++
        }
        return args
    }

    private fun parseExecution(value: Any): ScriptExecutor {
        require(value is String) {
            // 类型不匹配
            asLang("module-command-exception-invalid-content", id, "execute", value.javaClass.name)
        }
        require(value.isNotBlank()) {
            // 字符串内容为空
            asLang("module-command-exception-invalid-content", id, "execute", "BLANK#空白")
        }
        return ScriptExecutor(value, disableSuccessMessage, ::transformArgs)
    }

    private fun parseNodeChain(): List<Node> {
        val list = LinkedList<Node>()
        var parentNode: Node? = this
        while (parentNode != null && parentNode !is MainNode) {
            list.addFirst(parentNode)
            parentNode = parentNode.parent
        }
        return list
    }

}