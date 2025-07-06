package top.lanscarlos.vulpecula.module.volatility

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
interface VolatileEntityMetadata {

    fun updateHealth(viewer: Player, entity: Entity, health: Float)

    fun setGlowing(viewer: Player, entity: Entity, isGlowing: Boolean)

    companion object : VolatileEntityMetadata by nmsProxy()

}