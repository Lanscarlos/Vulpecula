package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.module.script.ScriptService
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/30 10:19
 */
class ScriptExecutor(source: String, val chain: List<Node>) : Suggester<Any>, Restrictor<Any>, Executor {

    val script = ScriptService.compile(source, "script-executor")

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
            context.args()
        } catch (_: Exception) {
            // 防止根命令获取空参数
            emptyArray<String>()
        }

        // 参数数量校验
        require(rawArgs.size == chain.size) { "Invalid args size: ${rawArgs.size}. It must be ${chain.size}." }

        // 参数转换
        val args = mutableMapOf<String, Any>("args" to rawArgs)
        for ((index, rawArg) in rawArgs.withIndex()) {
            args["arg$index"] = rawArgs
            val node = chain[index] as? DynamicNode ?: continue
            val arg = node.strategy?.convert(rawArg) ?: continue
            args[node.name] = arg
        }

        // 执行脚本
        return ScriptService.run(script, sender, args)
    }

}