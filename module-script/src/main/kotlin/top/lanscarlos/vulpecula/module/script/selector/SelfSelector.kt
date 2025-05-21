package top.lanscarlos.vulpecula.module.script.selector

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import top.lanscarlos.vulpecula.module.script.SenderSelector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script.selector
 *
 * @author Lanscarlos
 * @since 2025/5/21 14:09
 */
object SelfSelector : SenderSelector {
    override fun select(sender: ProxyCommandSender?): List<ProxyCommandSender> {
        return sender?.let(::listOf) ?: listOf(console()) // 若自身为空, 则以控制台为执行者
    }
}