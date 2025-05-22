package top.lanscarlos.vulpecula.module.schedule.selector

import org.bukkit.Bukkit
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptPlayer
import top.lanscarlos.vulpecula.module.schedule.SenderSelector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule.selector
 *
 * @author Lanscarlos
 * @since 2025/5/22 11:30
 */
class PlayerSelector(val name: String) : SenderSelector {
    override fun select(sender: ProxyCommandSender?): List<ProxyCommandSender> {
        return Bukkit.getPlayerExact(name)?.let(::adaptPlayer)?.let(::listOf)
            ?: error("无法选取脚本执行者, 玩家 $name 不在线.")
    }
}