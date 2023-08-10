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

    val HEALTH = volatile(11700 to 9, 11400 to 8, 11000 to 7, 10900 to 6)

    fun updateHealth(viewer: Player, entity: Entity, health: Float) {
        viewer.sendPacket(VolatilePacket.createPacketPlayOutEntityMetadata(entity.entityId, HEALTH to health))
    }

    private fun volatile(vararg meta: Pair<Int, Int>): Int {
        return (meta.firstOrNull { minecraftVersion >= it.first }?.second ?: -1)
    }
}