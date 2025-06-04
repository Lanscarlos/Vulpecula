package top.lanscarlos.vulpecula.common.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import top.lanscarlos.vulpecula.common.lang.MessageService.asErrorLiteral
import top.lanscarlos.vulpecula.common.lang.MessageService.asError
import top.lanscarlos.vulpecula.common.lang.MessageService.asInfoLiteral
import top.lanscarlos.vulpecula.common.lang.MessageService.asInfo
import top.lanscarlos.vulpecula.common.lang.MessageService.asWarningLiteral
import top.lanscarlos.vulpecula.common.lang.MessageService.asWarning
import top.lanscarlos.vulpecula.common.lang.MessageService.console

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.message
 *
 * @author Lanscarlos
 * @since 2025/5/6 15:44
 */

fun ProxyCommandSender.info(node: String, vararg args: Any) {
    sendMessage(asInfo(node, *args))
}

fun ProxyCommandSender.infoLiteral(message: String) {
    sendMessage(asInfoLiteral(message))
}

fun ProxyCommandSender.warning(node: String, vararg args: Any) {
    sendMessage(asWarning(node, *args))
}

fun ProxyCommandSender.warningLiteral(message: String) {
    sendMessage(asWarningLiteral(message))
}

fun ProxyCommandSender.error(node: String, vararg args: Any) {
    sendMessage(asError(node, *args))
}

fun ProxyCommandSender.errorLiteral(message: String) {
    sendMessage(asErrorLiteral(message))
}

fun ProxyCommandSender.infoSync(node: String, vararg args: Any) {
    val message = asInfo(node, *args)
    sendMessage(message)
    if (this is ProxyPlayer) {
        console.sendMessage(message)
    }
}

fun ProxyCommandSender.infoLiteralSync(message: String) {
    val msg = asInfoLiteral(message)
    sendMessage(msg)
    if (this is ProxyPlayer) {
        console.sendMessage(msg)
    }
}

fun ProxyCommandSender.warningSync(node: String, vararg args: Any) {
    val message = asWarning(node, *args)
    sendMessage(message)
    if (this is ProxyPlayer) {
        console.sendMessage(message)
    }
}

fun ProxyCommandSender.warningLiteralSync(message: String) {
    val msg = asWarningLiteral(message)
    sendMessage(msg)
    if (this is ProxyPlayer) {
        console.sendMessage(msg)
    }
}

fun ProxyCommandSender.errorSync(node: String, vararg args: Any) {
    val message = asError(node, *args)
    sendMessage(message)
    if (this is ProxyPlayer) {
        console.sendMessage(message)
    }
}

fun ProxyCommandSender.errorLiteralSync(message: String) {
    val msg = asErrorLiteral(message)
    sendMessage(msg)
    if (this is ProxyPlayer) {
        console.sendMessage(msg)
    }
}

/**
 * 输出同步日志, 且仅管理员和后台可见
 * */
fun ProxyCommandSender.logSync(message: String) {
    if (this !is ProxyPlayer) {
        this.sendMessage(message)
        return
    }
    if (this.isOp) {
        this.sendMessage(message)
    }
    console.sendMessage(message)
}