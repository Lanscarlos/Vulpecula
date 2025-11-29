package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.function.warning
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.config.boolean
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.config.string
import top.lanscarlos.vulpecula.common.config.stringOrNull
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 10:31
 */
abstract class Node(val id: String, val parent: Node?, config: ConfigurationSection) {

    val name: String by config.read("name").string(id)

    val permission by config.read("permission").string("")

    val optional by config.read("optional").boolean(false)

    val playerRequired by config.read("player-required").boolean(false)

    val disableSuccessMessage by config.read("disable-success-message").boolean(false)

    val children = LinkedList<Node>()

    /**
     * 父子关系链条
     * */
    val chain: List<Node>

    /**
     * 索引
     * */
    val index: Int

    open val executor: ScriptExecutor? by config.read("execute").stringOrNull().convert(::parseExecution)

    init {
        chain = parseNodeChain()
        index = chain.size - 1
    }

    abstract fun build(): CommandComponent

    open fun execute(sender: ProxyPlayer, context: CommandContext<ProxyPlayer>, argument: String) {
        val executor = executor
        if (executor == null) {
            warning("此处无执行器.")
            return
        }
        executor.execute(sender, context)
    }

    open fun execute(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String) {
        val executor = executor
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

    private fun parseExecution(value: String?): ScriptExecutor? {
        if (value == null) {
            return null
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