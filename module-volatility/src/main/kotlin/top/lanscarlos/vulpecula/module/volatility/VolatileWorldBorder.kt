package top.lanscarlos.vulpecula.module.volatility

import org.bukkit.Location
import org.bukkit.WorldBorder
import org.bukkit.entity.Player
import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/7
 */
interface VolatileWorldBorder {

    fun sendWorldBorder(viewer: Player, worldBorder: WorldBorder)

    fun sendWorldBorder(
        viewer: Player,
        center: Location,
        size: Double,
        warningTime: Int,
        warningDistance: Int,
        damageBuffer: Double,
        damageAmount: Double
    )

    fun sendDynamicWorldBorder(
        viewer: Player,
        center: Location,
        oldSize: Double,
        newSize: Double,
        speed: Long,
        warningTime: Int,
        warningDistance: Int,
        damageBuffer: Double,
        damageAmount: Double
    )

    companion object : VolatileWorldBorder by nmsProxy()

}