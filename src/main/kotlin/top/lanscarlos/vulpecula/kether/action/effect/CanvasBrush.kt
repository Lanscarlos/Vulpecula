package top.lanscarlos.vulpecula.kether.action.effect

import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.common.util.Vector
import java.awt.Color

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.effect
 *
 * 笔刷
 *
 * @author Lanscarlos
 * @since 2022-11-08 20:08
 */
class CanvasBrush {

    var particle = ProxyParticle.FLAME
    var count = 1
    var speed = 0.0
    var offset = Vector(0, 0, 0)
    var vector = Vector(0, 0, 0)

    var size: Float = 1f
    var color: Color = Color.WHITE
    var transition: Color = Color.WHITE
    var material: String = "STONE"
    var data: Int = 0
    var name: String = ""
    var lore: List<String> = emptyList()
    var customModelData: Int = -1

    fun draw(location: Location, viewers: Collection<ProxyPlayer>) {
        if (viewers.isEmpty()) return

        val meta = when (particle) {
            ProxyParticle.BLOCK_DUST -> ProxyParticle.BlockData(material, data)
            ProxyParticle.REDSTONE,
            ProxyParticle.SPELL_MOB,
            ProxyParticle.SPELL_MOB_AMBIENT -> ProxyParticle.DustData(color, size)
            ProxyParticle.DUST_COLOR_TRANSITION -> ProxyParticle.DustTransitionData(color, transition, size)
            ProxyParticle.ITEM_CRACK -> ProxyParticle.ItemData(material, data, name, lore, customModelData)
            else -> null
        }

        if ((offset.x == 0.0) && (offset.y == 0.0) && (offset.z == 0.0)) {
            viewers.forEach {
                it.sendParticle(particle, location, vector, count, speed, meta)
            }
        } else {
            viewers.forEach {
                it.sendParticle(particle, location.clone().add(offset), vector, count, speed, meta)
            }
        }
    }
}