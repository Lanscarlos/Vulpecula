package top.lanscarlos.vulpecula.module.volatility

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.sendPacket

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
object VolatileEntityMetadata {

    val HEALTH = volatile(11700 to 9, 11400 to 8, 11000 to 7, 10900 to 6)

    fun updateHealth(viewer: Player, entity: Entity, health: Float) {
        viewer.sendPacket(VolatilePacket.createPacketPlayOutEntityMetadata(entity.entityId, HEALTH to health))
    }

    private fun volatile(vararg metadata: Pair<Int, Int>): Int {
        for ((version, value) in metadata) {
            if (MinecraftVersion.versionId >= version) {
                return value
            }
        }
        return -1
    }

}