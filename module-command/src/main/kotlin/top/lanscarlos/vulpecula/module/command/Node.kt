package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.function.warning
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.applicative.applicativeBoolean
import top.lanscarlos.vulpecula.common.message.errorLiteralSync
import top.lanscarlos.vulpecula.common.message.errorSync
import top.lanscarlos.vulpecula.common.message.info
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.script.Script
import top.lanscarlos.vulpecula.module.script.ScriptService
import top.lanscarlos.vulpecula.module.script.exception.ScriptNotFoundException
import java.util.LinkedList
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

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

    open val script: Any? = section["execute"]?.toString()?.let(::parseScript)

    val children = LinkedList<Node>()

    /**
     * 父子关系链条
     * */
    val chain: List<Node>

    /**
     * 索引
     * */
    val index: Int

    val executor: Executor?

    init {
        val linkedList = LinkedList<Node>()
        var parentNode: Node? = this
        while (parentNode != null && parentNode !is MainNode) {
            linkedList.addFirst(parentNode)
            parentNode = parentNode.parent
        }
        chain = linkedList
        index = linkedList.size - 1
        executor = section["execute"]?.let(::parseExecution)
    }

    abstract fun build(): CommandComponent

    open fun execute(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String) {
        if (script == null) {
            warning("此处无执行器.")
            return
        }
        val rawArgs = getRawArgs(context)
        val args = transformArgs(rawArgs)
        val command = getCommand(context, rawArgs)
        try {
            execute(script!!, sender, args, onSuccess = { onSuccess(sender, command, it) }, onFailure = { onFailure("execute", sender, command, it) })
        } catch (ex: Exception) {
            if (ex !is ScriptNotFoundException) {
                ex.printStackTrace()
            }
            sender.errorSync("module-command-execute-failure", command)
            sender.errorLiteralSync(ex.localizedMessage)
        }
    }

//    protected fun applyExecutor(component: CommandComponent) {
//        if (executor != null) {
//            if (playerRequired) {
//                component.execute(bind = ProxyPlayer::class.java, function = executor::execute)
//            } else {
//                component.execute(bind = ProxyCommandSender::class.java, function = executor::execute)
//            }
//        }
//    }

    protected fun getRawArgs(context: CommandContext<*>): List<String> {
        return try {
            context.args().toList()
        } catch (_: Exception) {
            // 防止根命令获取空参数
            emptyList()
        }
    }

    protected fun getCommand(context: CommandContext<*>, rawArgs: List<String>): String {
        return "/${context.name} ${rawArgs.joinToString(" ")}"
    }

    protected fun transformArgs(rawArgs: List<String>): Map<String, Any> {
        val args = mutableMapOf<String, Any>("args" to rawArgs)
        for ((index, rawArg) in rawArgs.withIndex()) {
            args["arg$index"] = rawArg
        }
        return args
    }

    protected fun execute(
        script: Any,
        sender: ProxyCommandSender,
        args: Map<String, Any>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): CompletableFuture<Any?> {
        return when (script) {
            is String -> {
                ScriptService.run(script, sender, args, onSuccess = onSuccess, onFailure = onFailure)
            }
            is Script -> {
                ScriptService.run(script, sender, args, onSuccess = onSuccess, onFailure = onFailure)
            }
            else -> error("Unsupported script type: ${script.javaClass.name}")
        }
    }

    protected fun onSuccess(sender: ProxyCommandSender, command: String, value: Any?) {
        sender.info("module-command-execute-success", command, value.toString())
    }

    protected fun onFailure(action: String, sender: ProxyCommandSender, command: String, exception: BacikalRuntimeException): Any? {
        sender.errorSync("module-command-$action-failure", command)
        sender.errorLiteralSync(exception.getActionMessage())
        sender.errorLiteralSync(exception.getReasonMessage())
        sender.errorLiteralSync(exception.getDetailMessage())
        return null
    }

    protected fun parseScript(source: String): Any {
        require(source.isNotBlank()) { "Source cannot be blank." }
        return if (source.lowercase().startsWith("@script:")) {
            // 调用脚本
            source.substringAfter(':')
        } else {
            // 编译脚本
            ScriptService.compile(source, "script-executor")
        }
    }

    private fun parseExecution(value: Any): Executor {
        require(value is String) { "Execution content is not a String." }
        require(value.isNotBlank()) { "Execution content cannot be blank." }
        return ScriptExecutor(value, chain.drop(1).plus(this))
    }

}