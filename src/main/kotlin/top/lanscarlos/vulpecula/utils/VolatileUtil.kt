package top.lanscarlos.vulpecula.utils

import net.minecraft.server.v1_16_R3.*
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.craftbukkit.v1_16_R3.CraftParticle
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.nms.nmsProxy
import taboolib.module.nms.sendPacket
import taboolib.platform.type.BukkitPlayer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.volatile
 *
 * @author Lanscarlos
 * @since 2022-11-10 23:22
 */

interface VolatileHandler {
    companion object: VolatileHandler by nmsProxy()

    fun sendParticle(player: ProxyPlayer, particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: ProxyParticle.Data?)
}

class VolatileHandlerImpl : VolatileHandler {

    override fun sendParticle(player: ProxyPlayer, particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: ProxyParticle.Data?) {

        val bukkitPlayer = (player as BukkitPlayer).player

        var color: Color? = null
        val particles = CraftParticle.toNMS(
            Particle.valueOf(particle.name),
            when(data) {
                is ProxyParticle.DustData -> {
                    color = Color.fromRGB(data.color.red, data.color.green, data.color.blue)
                    Particle.DustOptions(color, data.size)
                }
                else -> null
            }
        )

        (bukkitPlayer as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutWorldParticles(
            particles, false, location.x, location.y, location.z,
            color?.red?.div(255f) ?: offset.x.toFloat(),
            color?.green?.div(255f) ?: offset.y.toFloat(),
            color?.blue?.div(255f) ?: offset.z.toFloat(),
            color?.let { 1f } ?: speed.toFloat(),
            color?.let { 0 } ?: count
        ))

//        val packet = setFields(
//            PacketPlayOutExperience(),
//            "a" to exp.coerceIn(0f, 1f),
//            "b" to player.totalExperience,
//            "c" to 0,
//        )
//        player.sendPacket(packet)
    }

//    private fun setFields(packet: Any, vararg content: Pair<String, Any?>): Any {
//        content.forEach {
//            packet.setProperty(it.first, it.second)
//        }
//        return packet
//    }
}