package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.common.message.MessageService
import top.lanscarlos.vulpecula.common.message.errorLiteralSync
import top.lanscarlos.vulpecula.common.message.errorSync
import top.lanscarlos.vulpecula.common.message.info
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.script.Script
import top.lanscarlos.vulpecula.module.script.ScriptService
import top.lanscarlos.vulpecula.module.script.exception.ScriptNotFoundException
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/30 10:19
 */
class ScriptExecutor(execution: String, private val chain: List<Node>) : Suggester<Any>, Restrictor<Any>, Executor {

    val script: Any = parseScript(execution)

    override fun <T : ProxyCommandSender> suggest(sender: T, context: CommandContext<T>): List<String> {
        val rawArgs = getRawArgs(context)
        val args = transformArgs(rawArgs)
        val command = getCommand(context, rawArgs)
        val result = execute(script, sender, command, args)

        require(result.isDone)
        return result.getNow(null).applicativeStringList(emptyList())
    }

    override fun <T : ProxyCommandSender> restrict(sender: T, context: CommandContext<T>, argument: String): Boolean {
        val rawArgs = getRawArgs(context)
        val args = transformArgs(rawArgs)
        val command = getCommand(context, rawArgs)
        val result = execute(script, sender, command, args)

        require(result.isDone)
        return result.getNow(null).applicativeBoolean(false)
    }

    override fun convert(input: String): Any {
        return input
    }

    override fun <T : ProxyCommandSender> execute(sender: T, context: CommandContext<T>, argument: String) {
        val rawArgs = getRawArgs(context)
        val args = transformArgs(rawArgs)
        val command = getCommand(context, rawArgs)
        try {
            execute(script, sender, command, args)
        } catch (ex: ScriptNotFoundException) {
            // Script not found.
            sender.errorSync("module-command-execute-failure", command)
            sender.errorLiteralSync(ex.localizedMessage)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun getRawArgs(context: CommandContext<*>): List<String> {
        return try {
            context.args().toList()
        } catch (_: Exception) {
            // 防止根命令获取空参数
            emptyList()
        }
    }

    private fun getCommand(context: CommandContext<*>, rawArgs: List<String>): String {
        return "/${context.name} ${rawArgs.joinToString(" ")}"
    }

    private fun transformArgs(rawArgs: List<String>): Map<String, Any> {
        val args = mutableMapOf<String, Any>("args" to rawArgs)
        for ((index, rawArg) in rawArgs.withIndex()) {
            args["arg$index"] = rawArg
            val node = chain[index] as? DynamicNode ?: continue
            val arg = node.converter?.convert(rawArg)
            args[node.name] = arg ?: rawArg
        }
        return args
    }

    private fun execute(script: Any, sender: ProxyCommandSender, command: String, args: Map<String, Any>): CompletableFuture<Any?> {
        return when (script) {
            is String -> {
                ScriptService.run(script, sender, args, onSuccess = { onSuccess(sender, command, it) }, onFailure = { onFailure(sender, command, it) })
            }
            is Script -> {
                ScriptService.run(script, sender, args, onSuccess = { onSuccess(sender, command, it) }, onFailure = { onFailure(sender, command, it) })
            }
            else -> error("Unsupported script type: ${script.javaClass.name}")
        }
    }

    private fun onSuccess(sender: ProxyCommandSender, command: String, value: Any?) {
        sender.info("module-command-execute-success", command, value.toString())
    }

    private fun onFailure(sender: ProxyCommandSender, command: String, exception: BacikalRuntimeException): Any? {
        sender.errorSync("module-command-execute-failure", command)
        sender.errorLiteralSync(exception.getActionMessage())
        sender.errorLiteralSync(exception.getReasonMessage())
        sender.errorLiteralSync(exception.getDetailMessage())
        return null
    }

    private fun parseScript(source: String): Any {
        require(source.isNotBlank()) { "Source cannot be blank." }
        return if (source.lowercase().startsWith("@script:")) {
            // 调用脚本
            source.substringAfter(':')
        } else {
            // 编译脚本
            ScriptService.compile(source, "script-executor")
        }
    }

}