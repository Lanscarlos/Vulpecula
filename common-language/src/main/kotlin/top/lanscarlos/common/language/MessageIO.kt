package top.lanscarlos.common.language

import taboolib.common.platform.ProxyCommandSender
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.common.language
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */

/**
 * 将节点和参数解析为语言消息
 * @param node 语言节点
 * @param args 消息参数
 * @return 解析后的消息
 */
fun asLang(node: String, vararg args: Any): Message {
    return Message(node, args.toList().toTypedArray(), null)
}

/**
 * 发送信息
 * @param module 模块名
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
fun ProxyCommandSender.info(
    module: String,
    sync: Boolean = false,
    message: Supplier<Message>
) {
    LanguageService.info(this, module, sync, message)
}

/**
 * 发送警告信息
 * @param module 模块名
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
fun ProxyCommandSender.warning(
    module: String,
    sync: Boolean = false,
    message: Supplier<Message>
) {
    LanguageService.warning(this, module, sync, message)
}

/**
 * 发送错误信息
 * @param module 模块名
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
fun ProxyCommandSender.error(
    module: String,
    sync: Boolean = false,
    message: Supplier<Message>
) {
    LanguageService.error(this, module, sync, message)
}

/**
 * 发送错误信息
 * @param module 模块名
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
fun ProxyCommandSender.debug(
    module: String,
    sync: Boolean = false,
    message: Supplier<Message>
) {
    LanguageService.debug(this, module, sync, message)
}