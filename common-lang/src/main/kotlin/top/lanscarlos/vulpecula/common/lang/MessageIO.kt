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
 * @since 2025/5/6 15:44
 */

private val console = console()

/**
 * 将节点和参数解析为语言消息
 * @param node 语言节点
 * @param args 消息参数
 * @return 解析后的消息
 */
fun asLang(node: String, vararg args: Any): String {
    return console.asLangText(node, *args)
}

/**
 * 发送信息消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
fun ProxyCommandSender.info(
    sync: Boolean = false,
    message: () -> String
) {
    val msg = asLang("common-lang-info", message())
    sendMessage(msg)
    if (sync && this is ProxyPlayer) {
        console.sendMessage(msg)
    }
}

/**
 * 发送警告消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
fun ProxyCommandSender.warning(
    sync: Boolean = false,
    message: () -> String
) {
    val msg = asLang("common-lang-warning", message())
    sendMessage(msg)
    if (sync && this is ProxyPlayer) {
        console.sendMessage(msg)
    }
}

/**
 * 发送错误消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
fun ProxyCommandSender.error(
    sync: Boolean = false,
    message: () -> String
) {
    val msg = asLang("common-lang-error", message())
    sendMessage(msg)
    if (sync && this is ProxyPlayer) {
        console.sendMessage(msg)
    }
}

/**
 * 发送调试消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
fun ProxyCommandSender.debug(
    sync: Boolean = false,
    message: () -> String
) {
    val msg = asLang("common-lang-debug", message())
    sendMessage(msg)
    if (sync && this is ProxyPlayer) {
        console.sendMessage(msg)
    }
}
