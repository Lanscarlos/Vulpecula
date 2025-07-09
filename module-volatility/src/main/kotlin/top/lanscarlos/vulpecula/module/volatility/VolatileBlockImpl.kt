package top.lanscarlos.vulpecula.module.volatility

import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.library.reflex.Reflex.Companion.unsafeInstance
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.sendPacket
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/9 10:15
 */
class VolatileBlockImpl : VolatileBlock {

    override fun sendBlockChange(viewer: Player, location: Location, data: BlockData) {
        viewer.sendPacket(createBlockChange(location, data))
    }

    override fun createBlockChange(location: Location, data: BlockData): Any {
        return if (MinecraftVersion.isUniversal) {
            NMSPacketPlayOutBlockChange(
                NMSBlockPosition(location.blockX, location.blockY, location.blockZ),
                (data as Craft21BlockData).state
            )
        } else {
            NMS16PacketPlayOutBlockChange(
                NMS16BlockPosition(location.blockX, location.blockY, location.blockZ),
                (data as Craft16BlockData).state
            )
        }
    }

    override fun createMultiBlockChange(data: List<Pair<Location, BlockData>>): Any {
        require(data.isNotEmpty()) { "Block data list is empty" }
        if (MinecraftVersion.isUniversal) {
            val chunks = mutableMapOf<NMSSectionPosition, LinkedList<Pair<Short, NMS21BlockData>>>()
            for ((location, blockData) in data) {
                val blockPosition = NMSBlockPosition(location.blockX, location.blockY, location.blockZ)
                val position = NMSSectionPosition.of(blockPosition)
                val chunkData = chunks.computeIfAbsent(position) { LinkedList() }
                val short = NMSSectionPosition.sectionRelativePos(blockPosition)
                chunkData.add(short to (blockData as Craft21BlockData).state)
            }
            return chunks.map { (key, data) ->
                NMSPacketPlayOutMultiBlockChange::class.java.unsafeInstance().also {
                    it.setProperty("sectionPos", key, remap = true)
                    it.setProperty("positions", ShortArray(data.size) { index -> data[index].first })
                    it.setProperty("states", Array(data.size) { index -> data[index].second })
                }
            }
        } else {
            val chunks = mutableMapOf<NMS16SectionPosition, LinkedList<Pair<Short, NMS16BlockData>>>()
            for ((location, blockData) in data) {
                val blockPosition = NMS16BlockPosition(location.blockX, location.blockY, location.blockZ)
                val position = NMS16SectionPosition.a(blockPosition)
                val chunkData = chunks.computeIfAbsent(position) { LinkedList() }
                val short = NMS16SectionPosition.b(blockPosition)
                chunkData.add(short to (blockData as Craft16BlockData).state)
            }
            return chunks.map { (key, data) ->
                NMS16PacketPlayOutMultiBlockChange()::class.java.unsafeInstance().also {
                    it.setProperty("sectionPos", key, remap = true)
                    it.setProperty("positions", ShortArray(data.size) { index -> data[index].first })
                    it.setProperty("states", Array(data.size) { index -> data[index].second })
                }
            }
        }
    }

}

typealias NMSBlockPosition = net.minecraft.core.BlockPosition
typealias NMSSectionPosition = net.minecraft.core.SectionPosition
typealias NMSPacketPlayOutBlockChange = net.minecraft.network.protocol.game.PacketPlayOutBlockChange
typealias NMSPacketPlayOutMultiBlockChange = net.minecraft.network.protocol.game.PacketPlayOutMultiBlockChange

typealias Craft21BlockData = org.bukkit.craftbukkit.v1_21_R3.block.data.CraftBlockData
typealias NMS21BlockData = net.minecraft.world.level.block.state.IBlockData

typealias Craft16BlockData = org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData
typealias NMS16BlockPosition = net.minecraft.server.v1_16_R3.BlockPosition
typealias NMS16BlockData = net.minecraft.server.v1_16_R3.IBlockData
typealias NMS16SectionPosition = net.minecraft.server.v1_16_R3.SectionPosition
typealias NMS16PacketPlayOutBlockChange = net.minecraft.server.v1_16_R3.PacketPlayOutBlockChange
typealias NMS16PacketPlayOutMultiBlockChange = net.minecraft.server.v1_16_R3.PacketPlayOutMultiBlockChange