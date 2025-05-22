package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender
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
            return when (selector.first().lowercase()) {
                "console" -> ConsoleSelector
                "players" -> OnlinePlayerSelector
                "world" -> {
                    require(selector.size == 2) { "世界名不能为空" }
                    WorldSelector(selector[1])
                }
                "range" -> {
                    require(selector.size == 3) { "不合法" }
                    RangeSelector(selector[1], selector[2])
                }
                "area" -> {
                    require(selector.size == 3) { "不合法" }
                    AreaSelector(selector[1], selector[2])
                }
                else -> error("Unknown sender: $value")
            }
        }

    }

}