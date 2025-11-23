package top.lanscarlos.vulpecula.module.schedule.selector

import org.bukkit.Bukkit
import org.bukkit.World
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptPlayer
import top.lanscarlos.vulpecula.common.exception.WorldNotFoundException
import top.lanscarlos.vulpecula.module.schedule.SenderSelector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule.selector
 *
 * @author Lanscarlos
 * @since 2025/5/22 11:40
 */
class WorldSelector(val name: String) : SenderSelector {

    val world: World = Bukkit.getWorld(name) ?: throw WorldNotFoundException(name)

    override fun select(sender: ProxyCommandSender?): List<ProxyCommandSender> {
        return world.players.map(::adaptPlayer)
    }
}