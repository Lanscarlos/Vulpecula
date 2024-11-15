package top.lanscarlos.vulpecula.wireshark

import org.bukkit.entity.Player
import taboolib.module.nms.Packet

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.wireshark
 *
 * @author Lanscarlos
 * @since 2024-11-12 21:00
 */
data class PacketCache(
    val packet: Packet,
    val player: Player,
    val time: Long = System.currentTimeMillis()
)