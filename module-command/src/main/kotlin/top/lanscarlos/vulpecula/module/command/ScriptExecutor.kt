package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException
import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.common.utils.onlyConsole
import top.lanscarlos.vulpecula.common.utils.withConsole
import top.lanscarlos.vulpecula.module.bacikal.exception.QuestRuntimeException
import top.lanscarlos.vulpecula.module.script.Script
import top.lanscarlos.vulpecula.module.script.ScriptService
import top.lanscarlos.vulpecula.module.script.exception.ScriptNotCompletedException
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
        val rawArgs: List<String> = getRawArgs(context) // 原始参数
        val args: Map<String, Any> = transformArgs.apply(rawArgs)
        val command: String = getCommand(context, rawArgs)
        try {
            return ScriptService.run(script, sender, rawArgs, args)
                .onFailure { it } // 执行异常时直接返回异常对象
                .get(ListApplicative)
                .map(StringApplicative::convert)
        } catch (e: Exception) {
            when (e) {
                is TypeConversionException -> {
                    val source = e.source
                    if (source !is QuestRuntimeException) {
                        // 转换类型异常
                        Lang.MODULE_COMMAND_SUGGEST_FAILURE_CONVERSION.error(sender.withConsole(), command)
                        return emptyList()
                    }

                    // 脚本运行异常
                    Lang.MODULE_COMMAND_SUGGEST_FAILURE.error(sender.withConsole(), command)
                    source.notice(sender.onlyConsole())
                }
                is ScriptNotCompletedException -> Lang.MODULE_COMMAND_SUGGEST_FAILURE_TIMEOUT.error(sender.withConsole(), command)
                is AbstractLocalizedException -> e.notice(sender.withConsole())
                else -> {
                    Lang.MODULE_COMMAND_SUGGEST_FAILURE.error(sender.withConsole(), command)
                    e.printStackTrace()
                }
            }
        }
        return emptyList()
    }

    override fun restrict(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, argument: String): Boolean {
        val rawArgs: List<String> = getRawArgs(context) // 原始参数
        val args: Map<String, Any> = transformArgs.apply(rawArgs)
        val command: String = getCommand(context, rawArgs)
        try {
            return ScriptService.run(script, sender, rawArgs, args)
                .onFailure { it } // 执行异常时直接返回异常对象
                .get(BooleanApplicative)
        } catch (e: Exception) {
            when (e) {
                is TypeConversionException -> {
                    val source = e.source
                    if (source !is QuestRuntimeException) {
                        // 转换类型异常
                        Lang.MODULE_COMMAND_RESTRICT_FAILURE_CONVERSION.error(sender.withConsole(), command)
                        return false
                    }

                    // 脚本运行异常
                    Lang.MODULE_COMMAND_RESTRICT_FAILURE.error(sender.withConsole(), command)
                    source.notice(sender.onlyConsole())
                }
                is ScriptNotCompletedException -> Lang.MODULE_COMMAND_RESTRICT_FAILURE_TIMEOUT.error(sender.withConsole(), command)
                is AbstractLocalizedException -> e.notice(sender.withConsole())
                else -> {
                    Lang.MODULE_COMMAND_RESTRICT_FAILURE.error(sender.withConsole(), command)
                    e.printStackTrace()
                }
            }
        }
        return false
    }

    fun execute(sender: ProxyCommandSender, context: CommandContext<*>) {
        val rawArgs: List<String> = getRawArgs(context) // 原始参数
        val args: Map<String, Any> = transformArgs.apply(rawArgs)
        val command: String = getCommand(context, rawArgs)
        try {
            ScriptService.run(script, sender, rawArgs, args)
                .onSuccess {
                    if (disableSuccessMessage) {
                        return@onSuccess
                    }
                    Lang.MODULE_COMMAND_EXECUTE_SUCCESS.info(sender.withConsole(), command)
                }
                .onFailure {
                    Lang.MODULE_COMMAND_EXECUTE_FAILURE.error(sender.withConsole(), command)
                    it.notice(sender.onlyConsole())
                }
        } catch (e: Exception) {
            when (e) {
                is AbstractLocalizedException -> {
                    e.notice(sender.withConsole())
                }
                else -> {
                    Lang.MODULE_COMMAND_EXECUTE_FAILURE.error(sender.withConsole(), command)
                    e.printStackTrace()
                }
            }
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

}