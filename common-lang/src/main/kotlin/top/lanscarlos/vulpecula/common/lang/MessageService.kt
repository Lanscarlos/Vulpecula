package top.lanscarlos.vulpecula.common.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.console
import taboolib.module.lang.asLangText

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.message
 *
 * @author Lanscarlos
 * @since 2025/5/6 13:58
 */
object MessageService {

    internal val console = console()

    fun asInfo(node: String, vararg args: Any): String {
        return asInfoLiteral(asLang(node, *args))
    }

    fun asInfoLiteral(message: String): String {
        return asLang("common-message-info", message)
    }

    fun asWarning(node: String, vararg args: Any): String {
        return asWarningLiteral(asLang(node, *args))
    }

    fun asWarningLiteral(message: String): String {
        return asLang("common-message-warning", message)
    }

    fun asError(node: String, vararg args: Any): String {
        return asErrorLiteral(asLang(node, *args))
    }

    fun asErrorLiteral(message: String): String {
        return asLang("common-message-error", message)
    }

    fun asLang(node: String, vararg args: Any): String {
        return console.asLangText(node, *args)
    }

    /**
     * 同步输出信息
     * 若 sender 是玩家, 则消息同步输出至控制台
     * 若 sender 为非管理员, 则屏蔽该消息
     * */
    fun logSync(sender: ProxyCommandSender, logs: List<String>) {
        for (log in logs) {
            logSync(sender, log)
        }
    }

    /**
     * 同步输出信息
     * 若 sender 是玩家, 则消息同步输出至控制台
     * 若 sender 为非管理员, 则屏蔽该消息
     * */
    fun logSync(sender: ProxyCommandSender, log: String) {
        if (sender !is ProxyPlayer) {
            sender.sendMessage(log)
            return
        }
        if (sender.isOp) {
            sender.sendMessage(log)
        }
        console.sendMessage(log)
    }

}
