package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import taboolib.module.lang.Language
import taboolib.module.lang.sendLang
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
class ScriptExecutor(source: String, private val chain: List<Node>) : Suggester<Any>, Restrictor<Any>, Executor {

    private val script: Any

    init {
        require(source.isNotBlank()) { "Source cannot be blank." }
        script = if (source[0] == '@') {
            require(!source.contains(' ') && !source.contains('\n')) { "Invalid source: $source" }
            // 调用脚本
            source.substring(1)
        } else {
            // 编译脚本
            ScriptService.compile(source, "script-executor")
        }
    }

    override fun <T : ProxyCommandSender> suggest(sender: T, context: CommandContext<T>): List<String> {
        val result = execute(sender, context)
        require(result.isDone)
        return result.getNow(null).applicativeStringList(emptyList())
    }

    override fun <T : ProxyCommandSender> restrict(sender: T, context: CommandContext<T>, argument: String): Boolean {
        val result = execute(sender, context)
        require(result.isDone)
        return result.getNow(null).applicativeBoolean(false)
    }

    override fun <T : ProxyCommandSender> execute(sender: T, context: CommandContext<T>, argument: String) {
        try {
            execute(sender, context)
        } catch (ex: ScriptNotFoundException) {
            // Script not found.
            MessageService.logSync(sender, ex.localizedMessage)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun convert(input: String): Any {
        return input
    }

    private fun <T : ProxyCommandSender> execute(sender: T, context: CommandContext<T>): CompletableFuture<Any?> {
        // 获取参数
        val rawArgs = try {
            context.args().toList()
        } catch (_: Exception) {
            // 防止根命令获取空参数
            emptyList()
        }

        // 参数数量校验
        require(rawArgs.size == chain.size) { "Invalid args size: ${rawArgs.size}. It must be ${chain.size}." }

        // 参数转换
        val args = mutableMapOf<String, Any>("args" to rawArgs)
        for ((index, rawArg) in rawArgs.withIndex()) {
            args["arg$index"] = rawArg
            val node = chain[index] as? DynamicNode ?: continue
            val arg = node.strategy?.convert(rawArg)
            args[node.name] = arg ?: rawArg
        }

        // 获取命令行
        val command = "/${context.name} ${rawArgs.joinToString(" ")}"

        // 执行脚本
        return when (script) {
            is String -> {
                ScriptService.run(script, sender, args, onSuccess = { onSuccess(sender, command, it) }, onFailure = { onFailure(sender, command, it) })
            }
            is Script -> ScriptService.run(script, sender, args, onSuccess = { onSuccess(sender, command, it) }, onFailure = { onFailure(sender, command, it) })
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

}