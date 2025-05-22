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
 * @since 2025/5/22 11:40
 */
class WorldSelector(val name: String) : SenderSelector {

    override fun select(sender: ProxyCommandSender?): List<ProxyCommandSender> {
        val world = Bukkit.getWorld(name)
            ?: error("无法解析世界 $name")
        return world.players.map(::adaptPlayer)
    }
}