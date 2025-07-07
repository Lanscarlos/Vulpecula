package top.lanscarlos.vulpecula.module.volatility

import net.minecraft.server.v1_16_R3.PacketDataSerializer
import org.bukkit.Location
import org.bukkit.WorldBorder
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld
import org.bukkit.entity.Player
import taboolib.common.platform.function.info
import taboolib.module.nms.DataSerializer
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.dataSerializerBuilder
import taboolib.module.nms.sendBundlePacket
import taboolib.module.nms.sendPacket
import java.util.EnumSet

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/7
 */
class VolatileWorldBorderImpl : VolatileWorldBorder {

    override fun sendWorldBorder(viewer: Player, worldBorder: WorldBorder) {
        sendWorldBorder(
            viewer,
            worldBorder.center,
            worldBorder.size,
            worldBorder.warningTime,
            worldBorder.warningDistance,
            worldBorder.damageBuffer,
            worldBorder.damageAmount
        )
    }

    override fun sendWorldBorder(
        viewer: Player,
        center: Location,
        size: Double,
        warningTime: Int,
        warningDistance: Int,
        damageBuffer: Double,
        damageAmount: Double
    ) {
        if (MinecraftVersion.isUniversal) {
            // 1.17+
            info("1.17+")
            val worldBorder = (viewer.world as CraftWorld).handle.worldBorder
            worldBorder.size = size
            worldBorder.setCenter(center.x, center.z)
            worldBorder.warningTime = warningTime
            worldBorder.warningBlocks = warningDistance
            worldBorder.damagePerBlock = damageAmount
            worldBorder.damageSafeZone = damageBuffer
            viewer.sendPacket(NMSClientboundInitializeBorderPacket(worldBorder))
            return
        }

        info("1.16-")
        val packetSetCenter = NMS16PacketPlayOutWorldBorder().a(
            dataSerializerBuilder {
                writeEnumSet(
                    EnumSet.of(NMS16PacketPlayOutWorldBorderAction.SET_CENTER),
                    NMS16PacketPlayOutWorldBorderAction::class.java
                )
                writeDouble(center.x)
                writeDouble(center.z)
            }.build() as PacketDataSerializer
        )
        val packetSetSize = NMS16PacketPlayOutWorldBorder().a(
            dataSerializerBuilder {
                writeEnumSet(
                    EnumSet.of(NMS16PacketPlayOutWorldBorderAction.SET_SIZE),
                    NMS16PacketPlayOutWorldBorderAction::class.java
                )
                writeDouble(size)
            }.build() as PacketDataSerializer
        )
        val packetSetWarningTime = NMS16PacketPlayOutWorldBorder().a(
            dataSerializerBuilder {
                writeEnumSet(
                    EnumSet.of(NMS16PacketPlayOutWorldBorderAction.SET_WARNING_TIME),
                    NMS16PacketPlayOutWorldBorderAction::class.java
                )
                writeVarInt(warningTime)
            }.build() as PacketDataSerializer
        )
        val packetSetWarningBlocks = NMS16PacketPlayOutWorldBorder().a(
            dataSerializerBuilder {
                writeEnumSet(
                    EnumSet.of(NMS16PacketPlayOutWorldBorderAction.SET_WARNING_BLOCKS),
                    NMS16PacketPlayOutWorldBorderAction::class.java
                )
                writeVarInt(warningDistance)
            }.build() as PacketDataSerializer
        )
        viewer.sendBundlePacket(packetSetCenter, packetSetSize, packetSetWarningTime, packetSetWarningBlocks)
    }

    override fun sendDynamicWorldBorder(
        viewer: Player,
        center: Location,
        oldSize: Double,
        newSize: Double,
        speed: Long,
        warningTime: Int,
        warningDistance: Int,
        damageBuffer: Double,
        damageAmount: Double
    ) {
        if (MinecraftVersion.isUniversal) {
            // 1.17+
            val worldBorder = NMSWorldBorder()
            worldBorder.lerpSizeBetween(oldSize, newSize, speed)
            worldBorder.setCenter(center.x, center.z)
            worldBorder.warningTime = warningTime
            worldBorder.warningBlocks = warningDistance
            worldBorder.damagePerBlock = damageAmount
            worldBorder.damageSafeZone = damageBuffer
            viewer.sendPacket(NMSClientboundInitializeBorderPacket(worldBorder))
            return
        }
        val packetSetCenter = NMS16PacketPlayOutWorldBorder().a(
            dataSerializerBuilder {
                writeEnumSet(
                    EnumSet.of(NMS16PacketPlayOutWorldBorderAction.SET_CENTER),
                    NMS16PacketPlayOutWorldBorderAction::class.java
                )
                writeDouble(center.x)
                writeDouble(center.z)
            }.build() as PacketDataSerializer
        )
        val packetSetSize = NMS16PacketPlayOutWorldBorder().a(
            dataSerializerBuilder {
                writeEnumSet(
                    EnumSet.of(NMS16PacketPlayOutWorldBorderAction.LERP_SIZE),
                    NMS16PacketPlayOutWorldBorderAction::class.java
                )
                writeDouble(oldSize)
                writeDouble(newSize)
                writeVarLong(speed)
            }.build() as PacketDataSerializer
        )
        val packetSetWarningTime = NMS16PacketPlayOutWorldBorder().a(
            dataSerializerBuilder {
                writeEnumSet(
                    EnumSet.of(NMS16PacketPlayOutWorldBorderAction.SET_WARNING_TIME),
                    NMS16PacketPlayOutWorldBorderAction::class.java
                )
                writeVarInt(warningTime)
            }.build() as PacketDataSerializer
        )
        val packetSetWarningBlocks = NMS16PacketPlayOutWorldBorder().a(
            dataSerializerBuilder {
                writeEnumSet(
                    EnumSet.of(NMS16PacketPlayOutWorldBorderAction.SET_WARNING_BLOCKS),
                    NMS16PacketPlayOutWorldBorderAction::class.java
                )
                writeVarInt(warningDistance)
            }.build() as PacketDataSerializer
        )
        viewer.sendBundlePacket(packetSetCenter, packetSetSize, packetSetWarningTime, packetSetWarningBlocks)
    }

    private fun DataSerializer.writeVarLong(value: Long) {
        var tempValue = value
        while ((tempValue and -128L) != 0L) { // 判断是否还有未写入的高位
            writeByte(((tempValue and 127L).toInt() or 128).toByte()) // 写入当前7位并设置标志位
            tempValue = tempValue ushr 7 // 无符号右移7位
        }
        writeByte(tempValue.toByte()) // 写入剩余的7位
    }

}

typealias NMSWorldBorder = net.minecraft.world.level.border.WorldBorder
typealias NMSClientboundInitializeBorderPacket = net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket

typealias NMS16PacketPlayOutWorldBorder = net.minecraft.server.v1_16_R3.PacketPlayOutWorldBorder
typealias NMS16PacketPlayOutWorldBorderAction = net.minecraft.server.v1_16_R3.PacketPlayOutWorldBorder.EnumWorldBorderAction