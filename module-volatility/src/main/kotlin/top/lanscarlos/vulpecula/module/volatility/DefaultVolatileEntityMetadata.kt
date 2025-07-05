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
class DefaultVolatileEntityMetadata : VolatileEntityMetadata {

    val FLAGS = 0
    val FLAG_GLOWING = 6
    val HEALTH = volatile(11700 to 9, 11400 to 8, 11000 to 7, 10900 to 6)

    override fun updateHealth(viewer: Player, entity: Entity, health: Float) {
        viewer.sendPacket(VolatilePacket.createPacketPlayOutEntityMetadata(entity.entityId, HEALTH to health))
    }

    override fun setGlowing(viewer: Player, entity: Entity, isGlowing: Boolean) {
        setFlag(viewer, entity, FLAG_GLOWING, isGlowing)
    }

    fun setFlag(viewer: Player, entity: Entity, flag: Int, value: Boolean) {
        val mask = 1 shl flag
        val flags = VolatileDataWatcher.getByteMetadata(entity, FLAGS).toInt()
        val newFlags = if (value) {
            flags or mask
        } else {
            flags and mask.inv()
        }
        viewer.sendPacket(VolatilePacket.createPacketPlayOutEntityMetadata(entity.entityId, FLAGS to newFlags))
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