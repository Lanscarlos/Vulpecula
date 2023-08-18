package top.lanscarlos.vulpecula.volatile

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.sendPacket

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.volatile
 *
 * @author Lanscarlos
 * @since 2023-08-10 11:32
 */
object VolatileEntityMetadata {

    private val minecraftVersion = MinecraftVersion.majorLegacy

    val HEALTH = volatile(17 to 9, 14 to 8, 10 to 7, 9 to 6)
    val POSE = volatile(14 to 6)

    fun updateHealth(viewer: Player, entity: Entity, health: Float) {
        viewer.sendPacket(VolatilePacket.createPacketPlayOutEntityMetadata(entity.entityId, HEALTH to health))
    }

    fun updatePose(viewer: Player, entity: Player, pose: VolatilePose) {
        viewer.sendPacket(VolatilePacket.createPacketPlayOutEntityMetadata(entity.entityId, POSE to pose))
    }

    private fun volatile(vararg meta: Pair<Int, Int>): Int {
        return (meta.firstOrNull { minecraftVersion >= it.first - 8 }?.second ?: -1)
    }
}