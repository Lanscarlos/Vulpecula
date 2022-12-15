package top.lanscarlos.vulpecula.utils

import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2022-08-21 16:47
 */

object Debug {

    val OFF = 0
    val NORMAL = 1
    val HIGH = 2
    val HIGHEST = 3
    val MONITOR = 4

    val level by bindConfigNode("debug") {
        when (it.toString().lowercase()) {
            "normal", "1" -> NORMAL
            "high", "2" -> HIGH
            "highest", "3" -> HIGHEST
            "monitor", "4" -> MONITOR
            else -> OFF
        }
    }

    fun Any.debug(message: String) {
        debug(NORMAL, message)
    }

    fun Any.debug(level: Int, message: String) {
        if (level > Debug.level) return
        if (Debug.level >= HIGHEST) {
            console().sendLang("Plugin-Debug-Detail", message, this.javaClass.simpleName)
        } else {
            console().sendLang("Plugin-Debug-Normal", message)
        }
    }
}