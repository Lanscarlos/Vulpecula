package top.lanscarlos.vulpecula.module.bacikal

import taboolib.common.platform.ProxyCommandSender
import top.lanscarlos.vulpecula.common.core.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal
 *
 * @author Lanscarlos
 * @since 2025/6/18
 */

/**
 * 发送信息消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
internal fun ProxyCommandSender.info(sync: Boolean = false, message: () -> String) = info(module = BacikalService.module, sync = sync, message = message)

/**
 * 发送警告消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
internal fun ProxyCommandSender.warning(sync: Boolean = false, message: () -> String) = warning(module = BacikalService.module, sync = sync, message = message)

/**
 * 发送错误消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
internal fun ProxyCommandSender.error(sync: Boolean = false, message: () -> String) = error(module = BacikalService.module, sync = sync, message = message)

/**
 * 发送调试消息
 * @param sync 是否同步到控制台
 * @param message 消息生成器
 */
internal fun ProxyCommandSender.debug(sync: Boolean = false, message: () -> String) = debug(module = BacikalService.module, sync = sync, message = message)
