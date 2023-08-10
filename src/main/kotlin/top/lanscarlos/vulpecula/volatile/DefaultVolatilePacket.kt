package top.lanscarlos.vulpecula.volatile

import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.library.reflex.Reflex.Companion.unsafeInstance
import taboolib.module.nms.MinecraftVersion

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.volatile
 *
 * @author Lanscarlos
 * @since 2023-08-10 11:07
 */
class DefaultVolatilePacket : VolatilePacket {

    private val isUniversal = MinecraftVersion.isUniversal
    private val minecraftVersion = MinecraftVersion.majorLegacy

    override fun createPacketPlayOutEntityMetadata(entityId: Int, vararg metadata: Pair<Int, Any>): Any {
        return if (minecraftVersion >= 11903) {
            NMSPacketPlayOutEntityMetadata(
                entityId,
                metadata.map { VolatileMetadata.deconstruct(it) as net.minecraft.network.syncher.DataWatcher.b<*> }
            )
        } else if (isUniversal) {
            NMSPacketPlayOutEntityMetadata::class.java.unsafeInstance().apply {
                setProperty("id", entityId)
                setProperty("packedItems", metadata.map { VolatileMetadata.deconstruct(it) })
            }
        } else {
            NMS16PacketPlayOutEntityMetadata::class.java.unsafeInstance().apply {
                setProperty("a", entityId)
                setProperty("b", metadata.map { VolatileMetadata.deconstruct(it) })
            }
        }
    }
}

typealias NMSPacketPlayOutEntityMetadata = net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata

typealias NMS16PacketPlayOutEntityMetadata = net.minecraft.server.v1_16_R1.PacketPlayOutEntityMetadata