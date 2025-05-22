package top.lanscarlos.vulpecula.module.schedule.selector

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.onlinePlayers
import top.lanscarlos.vulpecula.module.schedule.SenderSelector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule.selector
 *
 * @author Lanscarlos
 * @since 2025/5/22 11:40
 */
object OnlinePlayerSelector : SenderSelector {
    override fun select(sender: ProxyCommandSender?): List<ProxyCommandSender> {
        return onlinePlayers()
    }
}