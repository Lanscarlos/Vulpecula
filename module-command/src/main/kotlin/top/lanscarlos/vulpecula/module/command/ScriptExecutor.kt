package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.common.message.errorLiteralSync
import top.lanscarlos.vulpecula.common.message.errorSync
import top.lanscarlos.vulpecula.common.message.info
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.script.Script
import top.lanscarlos.vulpecula.module.script.ScriptService
import top.lanscarlos.vulpecula.module.script.exception.ScriptNotFoundException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/30 10:19
 */
class ScriptExecutor(
    execution: Any,
    private val transformArgs: Function<List<String>, Map<String, Any>>
) : Suggester, Restrictor {

    val script: Any = parseScript(execution)

    override fun suggest(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>): List<String> {
        val rawArgs = getRawArgs(context)
        val args = transformArgs(rawArgs)
        val command = getCommand(context, rawArgs)
        val future = execute(script, sender, args, onSuccess = {}, onFailure = { onFailure("suggest", sender, command, it) })

        if (!future.isDone) {
            sender.errorSync("module-command-suggest-failure", command)
            sender.errorSync("module-command-suggest-failure-timeout")
            return emptyList()
        }
        val result = future.getNow(null)
        val list = ListApplicative.convertOrNull(result)
        if (list == null) {
            sender.errorSync("module-command-suggest-failure", command)
            sender.errorSync("module-command-suggest-failure-conversion", result.toString())
            return emptyList()
        }
        return list.map { it.toString() }
    }

    override fun restrict(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String): Boolean {
        val rawArgs = getRawArgs(context)
        val args = transformArgs(rawArgs)
        val command = getCommand(context, rawArgs)
        val future = execute(script, sender, args, onSuccess = {}, onFailure = { onFailure("restrict", sender, command, it) })

        if (!future.isDone) {
            sender.errorSync("module-command-restrict-failure", command)
            sender.errorSync("module-command-restrict-failure-timeout")
            return false
        }
        val result = future.getNow(null)
        val boolean = BooleanApplicative.convertOrNull(result)
        if (boolean == null) {
            sender.errorSync("module-command-restrict-failure", command)
            sender.errorSync("module-command-restrict-failure-conversion", result.toString())
            return false
        }
        return boolean
    }

    fun execute(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>) {
        val rawArgs = getRawArgs(context)
        val args = transformArgs(rawArgs)
        val command = getCommand(context, rawArgs)
        try {
            execute(script, sender, args, onSuccess = { onSuccess(sender, command, it) }, onFailure = { onFailure("execute", sender, command, it) })
        } catch (ex: Exception) {
            if (ex !is ScriptNotFoundException) {
                ex.printStackTrace()
            }
            sender.errorSync("module-command-execute-failure", command)
            sender.errorLiteralSync(ex.localizedMessage)
        }
    }

    fun getRawArgs(context: CommandContext<*>): List<String> {
        return try {
            context.args().toList()
        } catch (_: Exception) {
            // 防止根命令获取空参数
            emptyList()
        }
    }

    fun getCommand(context: CommandContext<*>, rawArgs: List<String>): String {
        return "/${context.name} ${rawArgs.joinToString(" ")}"
    }

    fun transformArgs(rawArgs: List<String>): Map<String, Any> {
        return transformArgs.apply(rawArgs)
    }

    private fun execute(
        script: Any,
        sender: ProxyCommandSender,
        args: Map<String, Any>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): CompletableFuture<Any?> {
        return when (script) {
            is String -> {
                ScriptService.run(script, sender, args, onSuccess = onSuccess, onFailure = onFailure).future
            }
            is Script -> {
                ScriptService.run(script, sender, args, onSuccess = onSuccess, onFailure = onFailure).future
            }
            else -> error("Unsupported script type: ${script.javaClass.name}")
        }
    }

    private fun onSuccess(sender: ProxyCommandSender, command: String, value: Any?) {
        sender.info("module-command-execute-success", command, value.toString())
    }

    private fun onFailure(action: String, sender: ProxyCommandSender, command: String, exception: BacikalRuntimeException): Any? {
        sender.errorSync("module-command-$action-failure", command)
        sender.errorLiteralSync(exception.getActionMessage())
        sender.errorLiteralSync(exception.getReasonMessage())
        sender.errorLiteralSync(exception.getDetailMessage())
        return null
    }

    private fun parseScript(source: Any): Any {
        if (source is Script) {
            return source
        }
        require(source is String)
        require(source.isNotBlank())
        return if (source.lowercase().startsWith("@script:")) {
            // 调用脚本
            source.substringAfter(':')
        } else {
            // 编译脚本
            ScriptService.compile(source, "script-executor")
        }
    }

}