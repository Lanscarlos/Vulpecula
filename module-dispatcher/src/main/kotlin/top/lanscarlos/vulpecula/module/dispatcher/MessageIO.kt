package top.lanscarlos.vulpecula.module.dispatcher

import taboolib.common.platform.ProxyCommandSender
import top.lanscarlos.vulpecula.common.utils.debug
import top.lanscarlos.vulpecula.common.utils.error
import top.lanscarlos.vulpecula.common.utils.info
import top.lanscarlos.vulpecula.common.utils.warning

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/8
 */

/**
 * 发送信息消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
internal fun ProxyCommandSender.info(sync: Boolean = false, message: () -> String) = info(module = DispatcherService.name, sync = sync, message = message)

/**
 * 发送警告消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
internal fun ProxyCommandSender.warning(sync: Boolean = false, message: () -> String) = warning(module = DispatcherService.name, sync = sync, message = message)

/**
 * 发送错误消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
internal fun ProxyCommandSender.error(sync: Boolean = false, message: () -> String) = error(module = DispatcherService.name, sync = sync, message = message)

/**
 * 发送调试消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
internal fun ProxyCommandSender.debug(sync: Boolean = false, message: () -> String) = debug(module = DispatcherService.name, sync = sync, message = message)
