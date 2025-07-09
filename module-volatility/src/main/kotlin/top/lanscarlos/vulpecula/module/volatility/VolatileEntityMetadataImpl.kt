package top.lanscarlos.vulpecula.module.volatility

import net.minecraft.network.syncher.DataWatcher
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.library.reflex.Reflex.Companion.unsafeInstance
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.sendPacket
import top.lanscarlos.vulpecula.module.volatility.aliases.NMS16PacketPlayOutEntityMetadata
import top.lanscarlos.vulpecula.module.volatility.aliases.NMSPacketPlayOutEntityMetadata

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
class VolatileEntityMetadataImpl : VolatileEntityMetadata {

    val INDEX_FLAGS = 0
    val INDEX_HEALTH = volatile(11700 to 9, 11400 to 8, 11000 to 7, 10900 to 6)
    val INDEX_POSE = 6

    override fun setFlag(viewer: Player, entity: Entity, flag: EntityMetadataFlag, value: Boolean) {
        val mask = 1 shl flag.bit
        val flags = VolatileDataWatcher.getByteMetadata(entity, INDEX_FLAGS).toInt()
        val newFlags = if (value) {
            flags or mask
        } else {
            flags and mask.inv()
        }
        viewer.sendPacket(createPacketPlayOutEntityMetadata(entity.entityId, INDEX_FLAGS to newFlags.toByte()))
    }

    override fun setPose(viewer: Player, entity: Entity, pose: Pose) {
        viewer.sendPacket(createPacketPlayOutEntityMetadata(entity.entityId, INDEX_POSE to pose))
    }

    override fun updateHealth(viewer: Player, entity: Entity, health: Float) {
        viewer.sendPacket(createPacketPlayOutEntityMetadata(entity.entityId, INDEX_HEALTH to health))
    }

    /**
     * ink.ptms.adyeshach.impl.nms.DefaultMinecraftEntityMetadataHandler#createMetadataPacket
     * */
    private fun createPacketPlayOutEntityMetadata(entityId: Int, vararg metadata: Pair<Int, Any>): Any {
        return when {
            MinecraftVersion.versionId >= 11903 -> {
                // 1.19.3+ 变更为 record 类型，因此无法兼容之前的写法
                NMSPacketPlayOutEntityMetadata(
                    entityId,
                    metadata.map {
                        (VolatileDataWatcher.deconstruct(it) as DataWatcher.Item<*>).value()
                    }
                )
            }
            MinecraftVersion.isUniversal -> {
                // 1.17+
                NMSPacketPlayOutEntityMetadata::class.java.unsafeInstance().apply {
                    setProperty("id", entityId)
                    setProperty("packedItems", metadata.map { VolatileDataWatcher.deconstruct(it) })
                }
            }
            else -> {
                NMS16PacketPlayOutEntityMetadata::class.java.unsafeInstance().apply {
                    setProperty("a", entityId)
                    setProperty("b", metadata.map { VolatileDataWatcher.deconstruct(it) })
                }
            }
        }
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