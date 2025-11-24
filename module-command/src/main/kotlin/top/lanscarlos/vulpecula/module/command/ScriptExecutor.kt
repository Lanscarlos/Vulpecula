package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.function.info
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.common.utils.withConsole
import top.lanscarlos.vulpecula.module.bacikal.exception.QuestRuntimeException
import top.lanscarlos.vulpecula.module.script.Script
import top.lanscarlos.vulpecula.module.script.ScriptService
import top.lanscarlos.vulpecula.module.script.ScriptTask
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
    execution: String,
    val disableSuccessMessage: Boolean,
    private val transformArgs: Function<List<String>, Map<String, Any>>
) : Suggester, Restrictor {

    val script: Script = ScriptService.compile(execution)

    override fun suggest(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>): List<String> {
        try {
            val rawArgs = getRawArgs(context)
            val args = transformArgs(rawArgs)
            val command = getCommand(context, rawArgs)
            val future = ScriptService.run(script, sender, emptyList(), args)
                    .onFailure { onFailure("suggest", sender, command, it) }
                    .future

            if (!future.isDone) {
                sender.error(sync = true) { asLang("module-command-suggest-failure", command) }
                sender.error(sync = true) { asLang("module-command-suggest-failure-timeout") }
                return emptyList()
            }
            val result = future.getNow(null)
            info("我擦 result: $result")
            val list = ListApplicative.convertOrNull(result)
            if (list == null) {
                sender.error(sync = true) { asLang("module-command-suggest-failure", command) }
                sender.error(sync = true) { asLang("module-command-suggest-failure-conversion", result.toString()) }
                return emptyList()
            }
            info("我擦 list: $list")
            return list.map(Any?::toString)
        } catch (e: ScriptNotFoundException) {
            sender.error { "script ${e.id} not found" }
            e.notice(sender)
            return emptyList()
        } catch (e: Exception) {
            info("错误类型：${e.javaClass.name}")
            return emptyList()
        }
    }

    override fun restrict(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String): Boolean {
        val rawArgs = getRawArgs(context)
        val args = transformArgs(rawArgs)
        val command = getCommand(context, rawArgs)
        val future = execute(script, sender, args, onSuccess = {}, onFailure = { onFailure("restrict", sender, command, it) })

        if (!future.isDone) {
            sender.error(sync = true) { asLang("module-command-restrict-failure", command) }
            sender.error(sync = true) { asLang("module-command-restrict-failure-timeout") }
            return false
        }
        val result = future.getNow(null)
        val boolean = BooleanApplicative.convertOrNull(result)
        if (boolean == null) {
            sender.error(sync = true) { asLang("module-command-restrict-failure", command) }
            sender.error(sync = true) { asLang("module-command-restrict-failure-conversion", result.toString()) }
            return false
        }
        return boolean
    }

    fun execute(sender: ProxyCommandSender, context: CommandContext<*>) {
        val rawArgs = getRawArgs(context)
        val args = transformArgs(rawArgs)
        val command = getCommand(context, rawArgs)
        try {
            execute("execute", sender, context)
        } catch (ex: Exception) {
            if (ex !is ScriptNotFoundException) {
                ex.printStackTrace()
            }
            sender.error(sync = true) { asLang("module-command-execute-failure", command) }
            sender.error(sync = true) { ex.localizedMessage }
        }
    }

    private fun execute(action: String, sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>): ScriptTask {
        val rawArgs = getRawArgs(context) // 原始参数
        val args = transformArgs.apply(rawArgs)
        val command = "/${context.name} ${rawArgs.joinToString(" ")}"
        return ScriptService.run(script, sender, rawArgs, args)
            .onFailure {
                onFailure(action, sender, command, it)
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

    private fun onSuccess(sender: ProxyCommandSender, command: String, value: Any?) {
        if (disableSuccessMessage) {
            return
        }
        sender.info { asLang("module-command-execute-success", command.trim(), value.toString()) }
    }

    private fun onFailure(action: String, sender: ProxyCommandSender, command: String, exception: QuestRuntimeException): Any? {
        sender.error(sync = true) { asLang("module-command-$action-failure", command.trim()) }
        exception.notice(sender.withConsole())
        return null
    }

}