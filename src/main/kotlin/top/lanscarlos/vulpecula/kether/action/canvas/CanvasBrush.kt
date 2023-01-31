package top.lanscarlos.vulpecula.kether.action.canvas

import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.module.nms.MinecraftVersion
import java.awt.Color

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.canvas
 *
 * 笔刷
 *
 * @author Lanscarlos
 * @since 2022-11-08 20:08
 */
class CanvasBrush {

    var particle = ProxyParticle.FLAME
    var count = 1
    var speed = -1.0
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

    fun draw(locations: Collection<Location>, viewers: Collection<ProxyPlayer>) {
        locations.forEach { draw(it, viewers) }
    }

    fun draw(location: Location, viewers: Collection<ProxyPlayer>) {
        if (viewers.isEmpty()) return

        val meta = when (particle) {
            ProxyParticle.BLOCK_DUST -> ProxyParticle.BlockData(material, data)
            ProxyParticle.DUST_COLOR_TRANSITION -> ProxyParticle.DustTransitionData(color, transition, size)
            ProxyParticle.ITEM_CRACK -> ProxyParticle.ItemData(material, data, name, lore, customModelData)
            ProxyParticle.SPELL_MOB,
            ProxyParticle.SPELL_MOB_AMBIENT -> {
                // 默认调整为彩色粒子
                if (speed < 0) speed = 1.0
                null
            }
            ProxyParticle.REDSTONE -> {

                // 默认调整为彩色粒子
                if (speed < 0) speed = 1.0

                if (MinecraftVersion.major >= 5) {
                    // v1.13+
                    ProxyParticle.DustData(color, size)
                } else {
                    // v1.12 及以下
                    null
                }
            }
            else -> null
        }

        if ((offset.x == 0.0) && (offset.y == 0.0) && (offset.z == 0.0)) {
            viewers.forEach {
                it.sendParticle(particle, location, vector, count, speed.coerceAtLeast(0.0), meta)
            }
        } else {
            viewers.forEach {
                it.sendParticle(particle, location.clone().add(offset), vector, count, speed.coerceAtLeast(0.0), meta)
            }
        }
    }
}