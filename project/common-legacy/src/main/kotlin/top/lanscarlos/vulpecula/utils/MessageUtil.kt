package top.lanscarlos.vulpecula.utils

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2023-02-06 18:05
 */

/**
 * 同步控制台发送信息
 * 若发送者是玩家，则同时向控制台发送
 * */
fun CommandSender.sendSyncLang(node: String, vararg args: Any) {
    adaptCommandSender(this).sendLang(node, *args)
    if (this is Player) console().sendLang(node, *args)
}

/**
 * 发送静默消息
 * 仅发送于控制台
 * */
fun CommandSender.sendSyncLang(silent: Boolean, node: String, vararg args: Any) {
    if (silent) {
        console().sendLang(node, *args)
    } else {
        sendSyncLang(node, *args)
    }
}