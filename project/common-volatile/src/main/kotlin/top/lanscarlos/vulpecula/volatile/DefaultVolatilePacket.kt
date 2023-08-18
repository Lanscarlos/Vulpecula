package top.lanscarlos.vulpecula.volatile

import net.minecraft.network.syncher.DataWatcher
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.library.reflex.Reflex.Companion.unsafeInstance
import taboolib.module.nms.MinecraftVersion
import top.lanscarlos.vulpecula.volatile17.Volatile17Packet

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
            Volatile17Packet.createPacketPlayOutEntityMetadata(
                entityId,
                *metadata.map { VolatileMetadata.deconstruct(it) }.toTypedArray()
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