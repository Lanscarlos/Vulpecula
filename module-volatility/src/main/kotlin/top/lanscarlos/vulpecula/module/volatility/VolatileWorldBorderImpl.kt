package top.lanscarlos.vulpecula.module.volatility

import net.minecraft.server.v1_16_R3.PacketDataSerializer
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld
import org.bukkit.entity.Player
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

    override fun sendWorldBorder(
        viewer: Player,
        size: Double?,
        center: Location?,
        warningTime: Int?,
        warningDistance: Int?,
        damageBuffer: Double?,
        damageAmount: Double?
    ) {
        val packet = createWorldBorderPacket(viewer.world, size, center, warningTime, warningDistance, damageBuffer, damageAmount)
        if (MinecraftVersion.isUniversal) {
            // 1.17+
            viewer.sendPacket(packet)
        } else {
            // 1.16-
            val packets = (packet as List<*>).filterNotNull()
            viewer.sendBundlePacket(packets)
        }
    }

    override fun sendDynamicWorldBorder(
        viewer: Player,
        oldSize: Double,
        newSize: Double,
        speed: Long,
        center: Location?,
        warningTime: Int?,
        warningDistance: Int?,
        damageBuffer: Double?,
        damageAmount: Double?
    ) {
        val packet = createDynamicWorldBorderPacket(viewer.world, oldSize, newSize, speed, center, warningTime, warningDistance, damageBuffer, damageAmount)
        if (MinecraftVersion.isUniversal) {
            // 1.17+
            viewer.sendPacket(packet)
        } else {
            // 1.16-
            val packets = (packet as List<*>).filterNotNull()
            viewer.sendBundlePacket(packets)
        }
    }

    override fun createWorldBorderPacket(
        world: World,
        size: Double?,
        center: Location?,
        warningTime: Int?,
        warningDistance: Int?,
        damageBuffer: Double?,
        damageAmount: Double?
    ): Any {
        if (MinecraftVersion.isUniversal) {
            // 1.17+
            val worldBorder = (world as CraftWorld).handle.worldBorder
            if (size != null) {
                worldBorder.size = size
            }
            if (center != null) {
                worldBorder.setCenter(center.x, center.z)
            }
            if (warningTime != null) {
                worldBorder.warningTime = warningTime
            }
            if (warningDistance != null) {
                worldBorder.warningBlocks = warningDistance
            }
            if (damageBuffer != null) {
                worldBorder.damageSafeZone = damageBuffer
            }
            if (damageAmount != null) {
                worldBorder.damagePerBlock = damageAmount
            }
            return NMSClientboundInitializeBorderPacket(worldBorder)
        } else {
            // 1.16-
            val packets = mutableListOf<Any>()
            if (center != null) {
                packets += createNMS16SetCenterPacket(center)
            }
            if (size != null) {
                packets += createNMS16SetSizePacket(size)
            }
            if (warningTime != null) {
                packets += createNMS16SetWarningTimePacket(warningTime)
            }
            if (warningDistance != null) {
                packets += createNMS16SetWarningBlocksPacket(warningDistance)
            }
            return packets
        }
    }

    override fun createDynamicWorldBorderPacket(
        world: World,
        oldSize: Double,
        newSize: Double,
        speed: Long,
        center: Location?,
        warningTime: Int?,
        warningDistance: Int?,
        damageBuffer: Double?,
        damageAmount: Double?
    ): Any {
        if (MinecraftVersion.isUniversal) {
            // 1.17+
            val worldBorder = (world as CraftWorld).handle.worldBorder
            worldBorder.lerpSizeBetween(oldSize, newSize, speed)
            if (center != null) {
                worldBorder.setCenter(center.x, center.z)
            }
            if (warningTime != null) {
                worldBorder.warningTime = warningTime
            }
            if (warningDistance != null) {
                worldBorder.warningBlocks = warningDistance
            }
            if (damageBuffer != null) {
                worldBorder.damageSafeZone = damageBuffer
            }
            if (damageAmount != null) {
                worldBorder.damagePerBlock = damageAmount
            }
            return NMSClientboundInitializeBorderPacket(worldBorder)
        } else {
            // 1.16-
            val packets = mutableListOf(
                createNMS16SetLerpSizePacket(oldSize, newSize, speed)
            )
            if (center != null) {
                packets += createNMS16SetCenterPacket(center)
            }
            if (warningTime != null) {
                packets += createNMS16SetWarningTimePacket(warningTime)
            }
            if (warningDistance != null) {
                packets += createNMS16SetWarningBlocksPacket(warningDistance)
            }
            return packets
        }
    }

    private fun createNMS16SetCenterPacket(center: Location): Any {
        return NMS16PacketPlayOutWorldBorder().a(
            dataSerializerBuilder {
                writeEnumSet(
                    EnumSet.of(NMS16PacketPlayOutWorldBorderAction.SET_CENTER),
                    NMS16PacketPlayOutWorldBorderAction::class.java
                )
                writeDouble(center.x)
                writeDouble(center.z)
            }.build() as PacketDataSerializer
        )
    }

    private fun createNMS16SetSizePacket(size: Double): Any {
        return NMS16PacketPlayOutWorldBorder().a(
            dataSerializerBuilder {
                writeEnumSet(
                    EnumSet.of(NMS16PacketPlayOutWorldBorderAction.SET_SIZE),
                    NMS16PacketPlayOutWorldBorderAction::class.java
                )
                writeDouble(size)
            }.build() as PacketDataSerializer
        )
    }

    private fun createNMS16SetLerpSizePacket(oldSize: Double, newSize: Double, speed: Long): Any {
        return NMS16PacketPlayOutWorldBorder().a(
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
    }

    private fun createNMS16SetWarningTimePacket(warningTime: Int): Any {
        return NMS16PacketPlayOutWorldBorder().a(
            dataSerializerBuilder {
                writeEnumSet(
                    EnumSet.of(NMS16PacketPlayOutWorldBorderAction.SET_WARNING_TIME),
                    NMS16PacketPlayOutWorldBorderAction::class.java
                )
                writeVarInt(warningTime)
            }.build() as PacketDataSerializer
        )
    }

    private fun createNMS16SetWarningBlocksPacket(warningDistance: Int): Any {
        return NMS16PacketPlayOutWorldBorder().a(
            dataSerializerBuilder {
                writeEnumSet(
                    EnumSet.of(NMS16PacketPlayOutWorldBorderAction.SET_WARNING_BLOCKS),
                    NMS16PacketPlayOutWorldBorderAction::class.java
                )
                writeVarInt(warningDistance)
            }.build() as PacketDataSerializer
        )
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