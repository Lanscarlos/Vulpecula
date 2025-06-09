package top.lanscarlos.vulpecula.module.schedule.selector

import org.bukkit.Location
import org.bukkit.World
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptPlayer
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.common.applicative.LocationApplicative
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.schedule.SenderSelector
import kotlin.math.pow

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule.selector
 *
 * @author Lanscarlos
 * @since 2025/5/22 11:45
 */
class RangeSelector(location: String, range: String) : SenderSelector {

    val center: Location = LocationApplicative.convertOrThrow(location).toBukkitLocation()

    val range = range.toDouble().pow(2)

    val world: World = center.world ?: error(asLang("module-schedule-exception-invalid-location", location))

    override fun select(sender: ProxyCommandSender?): List<ProxyCommandSender> {
        return world.players.filter { it.location.distanceSquared(center) <= range }.map(::adaptPlayer)
    }

}