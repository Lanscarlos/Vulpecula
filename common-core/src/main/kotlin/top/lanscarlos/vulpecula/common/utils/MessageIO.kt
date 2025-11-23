package top.lanscarlos.vulpecula.common.utils

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.console
import taboolib.module.lang.asLangText

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.utils
 *
 * @author Lanscarlos
 * @since 2025/5/6 15:44
 */

private val console = console()

/**
 * 将当前对象与控制台相结合以实现同步消息
 * */
fun ProxyCommandSender.withConsole(): ProxyCommandSender {
    if (this !is ProxyPlayer) {
        return this
    }
    return MessageSyncCommandSender(this)
}

private class MessageSyncCommandSender(val player: ProxyPlayer) : ProxyCommandSender by player {

    override fun sendMessage(message: String) {
        player.sendMessage(message)
        console.sendMessage(message)
    }

}

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
 * @param module 模块名
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
fun ProxyCommandSender.info(
    module: String,
    sync: Boolean = false,
    message: () -> String
) {
    val msg = asLang("common-core-message-info", module, message())
    sendMessage(msg)
    if (sync && this is ProxyPlayer) {
        console.sendMessage(msg)
    }
}

/**
 * 发送警告消息
 * @param module 模块名
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
fun ProxyCommandSender.warning(
    module: String,
    sync: Boolean = false,
    message: () -> String
) {
    val msg = asLang("common-core-message-warning", module, message())
    sendMessage(msg)
    if (sync && this is ProxyPlayer) {
        console.sendMessage(msg)
    }
}

/**
 * 发送错误消息
 * @param module 模块名
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
fun ProxyCommandSender.error(
    module: String,
    sync: Boolean = false,
    message: () -> String
) {
    val msg = asLang("common-core-message-error", module, message())
    sendMessage(msg)
    if (sync && this is ProxyPlayer) {
        console.sendMessage(msg)
    }
}

/**
 * 发送调试消息
 * @param module 模块名
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
fun ProxyCommandSender.debug(
    module: String,
    sync: Boolean = false,
    message: () -> String
) {
    val msg = asLang("common-core-message-debug", module, message())
    sendMessage(msg)
    if (sync && this is ProxyPlayer) {
        console.sendMessage(msg)
    }
}
