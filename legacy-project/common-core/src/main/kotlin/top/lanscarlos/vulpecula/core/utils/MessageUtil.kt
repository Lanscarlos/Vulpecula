package top.lanscarlos.vulpecula.core.utils

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.utils
 *
 * @author Lanscarlos
 * @since 2023-08-27 13:59
 */

fun ProxyCommandSender.infoLang(node: String, vararg args: String) {
    if (this is ProxyPlayer) {
        this.sendLang(node, *args)
    }
    console().sendLang(node, *args)
}