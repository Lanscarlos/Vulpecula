package top.lanscarlos.vulpecula.common.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.Level
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendError
import taboolib.module.lang.sendInfo
import taboolib.module.lang.sendWarn
import top.lanscarlos.vulpecula.common.config.bindConfig
import top.lanscarlos.vulpecula.common.config.default
import top.lanscarlos.vulpecula.common.config.string
import top.lanscarlos.vulpecula.common.config.stringOrNull

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.lang
 *
 * @author Lanscarlos
 * @since 2025/11/22
 */
enum class Lang {

    MODULE_COMMAND_MISSING_ARGUMENT,

    MODULE_SCRIPT_LOAD_SUCCESS,

    EXCEPTION_QUEST_COMPILE_FAILURE, // 编译异常

    EXCEPTION_QUEST_EXECUTE_FAILURE, // 执行异常

    EXCEPTION_QUEST_ACTION, // 异常语句
    EXCEPTION_QUEST_REASON, // 异常原因
    EXCEPTION_QUEST_LOCATION_HEADER, // 异常位置
    EXCEPTION_QUEST_LOCATION_BODY,
    EXCEPTION_QUEST_LOCATION_FOOTER,

    EXCEPTION_SCRIPT_LOAD_FAILURE,

    EXCEPTION_SCRIPT_NOT_FOUND;

    val path: String = name.lowercase().replace('_', '-')

    fun asText(receiver: ProxyCommandSender, vararg args: Any): String {
        return receiver.asLangText(node = path, args = args)
    }

    fun info(receiver: ProxyCommandSender, vararg args: Any) {
        receiver.sendInfo(node = path, args = args)
    }

    fun warn(receiver: ProxyCommandSender, vararg args: Any) {
        receiver.sendWarn(node = path, args = args)
    }

    fun error(receiver: ProxyCommandSender, vararg args: Any) {
        receiver.sendError(node = path, args = args)
    }

}