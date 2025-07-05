package top.lanscarlos.vulpecula.module.volatility

import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.library.reflex.Reflex.Companion.unsafeInstance
import taboolib.module.nms.MinecraftVersion

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
class DefaultVolatilePacket : VolatilePacket {

    override fun createPacketPlayOutEntityMetadata(entityId: Int, vararg metadata: Pair<Int, Any>): Any {
        return when {
            MinecraftVersion.versionId >= 11903 -> {
                // 1.19.3+
                NMSPacketPlayOutEntityMetadata(
                    entityId,
                    metadata.map { VolatileDataWatcher.deconstruct(it) as net.minecraft.network.syncher.DataWatcher.b<*> }
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
}

typealias NMSPacketPlayOutEntityMetadata = net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata

typealias NMS16PacketPlayOutEntityMetadata = net.minecraft.server.v1_16_R1.PacketPlayOutEntityMetadata