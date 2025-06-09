package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender
import top.lanscarlos.vulpecula.common.core.exception.InvalidArgumentFormatException
import top.lanscarlos.vulpecula.common.core.exception.InvalidTypeException
import top.lanscarlos.vulpecula.module.schedule.selector.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * @author Lanscarlos
 * @since 2025/5/22 11:24
 */
interface SenderSelector {

    fun select(sender: ProxyCommandSender?): List<ProxyCommandSender>

    companion object {

        fun parse(value: String): SenderSelector {
            if (value.first() != '@') {
                // 指定在线玩家
                return PlayerSelector(value)
            }
            val selector = value.substring(1).split(' ')
            return when (val type = selector.first().lowercase()) {
                "console" -> ConsoleSelector
                "players" -> OnlinePlayerSelector
                "world" -> {
                    require(selector.size == 2) {

                    }
                    WorldSelector(selector[1])
                }
                "range" -> {
                    if (selector.size != 3) {
                        // 参数个数不匹配
                        throw InvalidArgumentFormatException(value)
                    }
                    RangeSelector(selector[1], selector[2])
                }
                "area" -> {
                    if (selector.size != 3) {
                        // 参数个数不匹配
                        throw InvalidArgumentFormatException(value)
                    }
                    AreaSelector(selector[1], selector[2])
                }
                else -> throw InvalidTypeException(type)
            }
        }

    }

}