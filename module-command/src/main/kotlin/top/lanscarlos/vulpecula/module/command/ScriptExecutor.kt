package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.script.Script
import top.lanscarlos.vulpecula.module.script.ScriptService
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
        execute(sender, context)
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

        // 执行脚本
        return when (script) {
            is String -> ScriptService.run(script, sender, args, onSuccess = { onSuccess(sender, it) }, onFailure = { onFailure(sender, it) })
            is Script -> ScriptService.run(script, sender, args, onSuccess = { onSuccess(sender, it) }, onFailure = { onFailure(sender, it) })
            else -> error("Unsupported script type: ${script.javaClass.name}")
        }
    }

    private fun onSuccess(sender: ProxyCommandSender, value: Any?) {

    }

    private fun onFailure(sender: ProxyCommandSender, exception: BacikalRuntimeException): Any {
        val logs = listOf(
            exception.getActionMessage(),
            exception.getReasonMessage(),
            exception.getDetailMessage()
        )
        if (sender is ProxyPlayer && sender.isOp) {
            // 向 OP 输出报错详情
            for (log in logs) {
                sender.sendMessage(log)
            }
        }
        // 报错同步至控制台
        for (log in logs) {
            console().sendMessage(log)
        }
        return false
    }

}