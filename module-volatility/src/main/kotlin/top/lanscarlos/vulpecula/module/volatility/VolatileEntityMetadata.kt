package top.lanscarlos.vulpecula.module.volatility

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
interface VolatileEntityMetadata {

    fun setGlowing(viewer: Player, entity: Entity, value: Boolean) {
        setFlag(viewer, entity, EntityMetadataFlag.GLOWING, value)
    }

    fun setFlag(viewer: Player, entity: Entity, flag: EntityMetadataFlag, value: Boolean)

    fun setPose(viewer: Player, entity: Entity, pose: Pose)

    fun updateHealth(viewer: Player, entity: Entity, health: Float)

    fun mount(viewer: Player, entity: Entity)

    companion object : VolatileEntityMetadata by nmsProxy()

}