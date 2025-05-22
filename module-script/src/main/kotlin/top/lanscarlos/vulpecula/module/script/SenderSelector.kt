package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import top.lanscarlos.vulpecula.module.script.selector.SelfSelector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025/5/21 14:04
 */
interface SenderSelector {

    fun select(sender: ProxyCommandSender?): List<ProxyCommandSender>

    companion object {

        fun parse(value: String): SenderSelector {
            return SelfSelector
        }

    }

}