package top.lanscarlos.vulpecula.module.schedule.selector

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptPlayer
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.common.applicative.LocationApplicative
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.module.schedule.SenderSelector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule.selector
 *
 * @author Lanscarlos
 * @since 2025/5/22 13:19
 */
class AreaSelector(location1: String, location2: String) : SenderSelector {

    val world: World

    val boundingBox: BoundingBox

    init {
        val a: Location = LocationApplicative.convertOrThrow(location1).toBukkitLocation()
        val b: Location = LocationApplicative.convertOrThrow(location2).toBukkitLocation()
        require(a.world != null) {
            asLang("module-schedule-exception-invalid-location", location1)
        }
        require(b.world != null) {
            asLang("module-schedule-exception-invalid-location", location2)
        }
        require(a.world == b.world) {
            asLang("module-schedule-exception-invalid-location")
        }
        world = a.world!!
        boundingBox = BoundingBox.of(a, b)
    }

    override fun select(sender: ProxyCommandSender?): List<ProxyCommandSender> {
        return world.players.filter { boundingBox.contains(it.location.x, it.location.y, it.location.z) }.map(::adaptPlayer)
    }

}