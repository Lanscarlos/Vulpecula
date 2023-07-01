package top.lanscarlos.vulpecula.bacikal.action.canvas.fx

import org.bukkit.entity.Entity
import taboolib.common.util.Vector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * 实体朝向
 *
 * @author Lanscarlos
 * @since 2023-06-30 01:37
 */
class EntityDirectionFx(val entity: Entity) : VectorFx<Entity>() {

    override fun calculate(): Vector {
        return calculate(entity)
    }

    override fun calculate(input: Entity): Vector {
        return input.location.direction.let { Vector(it.x, it.y, it.z) }
    }

    override fun copy(): Fx<Entity, Vector> {
        // 无需克隆
        return this
    }
}