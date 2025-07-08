package top.lanscarlos.vulpecula.module.volatility

import org.bukkit.Location
import org.bukkit.World
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

    fun sendWorldBorder(
        viewer: Player,
        size: Double?,
        center: Location?,
        warningTime: Int?,
        warningDistance: Int?,
        damageBuffer: Double?,
        damageAmount: Double?
    )

    fun sendDynamicWorldBorder(
        viewer: Player,
        oldSize: Double,
        newSize: Double,
        speed: Long,
        center: Location?,
        warningTime: Int?,
        warningDistance: Int?,
        damageBuffer: Double?,
        damageAmount: Double?
    )

    fun createWorldBorderPacket(
        world: World,
        size: Double?,
        center: Location?,
        warningTime: Int?,
        warningDistance: Int?,
        damageBuffer: Double?,
        damageAmount: Double?
    ): Any

    fun createDynamicWorldBorderPacket(
        world: World,
        oldSize: Double,
        newSize: Double,
        speed: Long,
        center: Location?,
        warningTime: Int?,
        warningDistance: Int?,
        damageBuffer: Double?,
        damageAmount: Double?
    ): Any

    companion object : VolatileWorldBorder by nmsProxy()

}