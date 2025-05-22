package top.lanscarlos.vulpecula.module.schedule.selector

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import top.lanscarlos.vulpecula.module.schedule.SenderSelector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule.selector
 *
 * @author Lanscarlos
 * @since 2025/5/22 11:25
 */
object SelfSelector : SenderSelector {
    override fun select(sender: ProxyCommandSender?): List<ProxyCommandSender> {
        return sender?.let(::listOf) ?: listOf(console())
    }
}