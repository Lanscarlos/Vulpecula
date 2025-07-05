package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import top.lanscarlos.common.language.Message
import top.lanscarlos.common.language.debug
import top.lanscarlos.common.language.error
import top.lanscarlos.common.language.info
import top.lanscarlos.common.language.warning
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025/6/8
 */

/**
 * 发送信息消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
internal fun ProxyCommandSender.info(sync: Boolean = false, message: Supplier<Message>) = info(module = ScriptService.name, sync = sync, message = message)

/**
 * 发送警告消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
internal fun ProxyCommandSender.warning(sync: Boolean = false, message: Supplier<Message>) = warning(module = ScriptService.name, sync = sync, message = message)

/**
 * 发送错误消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
internal fun ProxyCommandSender.error(sync: Boolean = false, message: Supplier<Message>) = error(module = ScriptService.name, sync = sync, message = message)

/**
 * 发送调试消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
internal fun ProxyCommandSender.debug(sync: Boolean = false, message: Supplier<Message>) = debug(module = ScriptService.name, sync = sync, message = message)
