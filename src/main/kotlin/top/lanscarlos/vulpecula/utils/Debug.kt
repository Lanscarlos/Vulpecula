package top.lanscarlos.vulpecula.utils

import taboolib.common.platform.function.console
import taboolib.module.configuration.Configuration
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

    var level = OFF
        private set

    fun setLevel(level: Int) {
        this.level = if (level <= OFF) {
            OFF
        } else if (level >= MONITOR) {
            MONITOR
        } else {
            level
        }
    }

    fun Any.debug(message: String) {
        debug(NORMAL, message)
    }

    fun Any.debug(level: Int, message: String) {
        if (level > Debug.level) return
        if (Debug.level >= HIGHEST) {
            console().sendLang("Plugin-Debug-Detail", this.javaClass.simpleName, message)
        } else {
            console().sendLang("Plugin-Debug-Normal", message)
        }
    }

    fun load(config: Configuration) {
        level = when (config.getString("debug")?.lowercase()) {
            "normal" -> NORMAL
            "high" -> HIGH
            "highest" -> HIGHEST
            "monitor" -> MONITOR
            else -> OFF
        }
    }
}
