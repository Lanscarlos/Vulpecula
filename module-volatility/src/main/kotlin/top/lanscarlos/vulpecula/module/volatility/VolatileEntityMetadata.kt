package top.lanscarlos.vulpecula.module.volatility

import org.bukkit.entity.Entity
import org.bukkit.entity.Player

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

}