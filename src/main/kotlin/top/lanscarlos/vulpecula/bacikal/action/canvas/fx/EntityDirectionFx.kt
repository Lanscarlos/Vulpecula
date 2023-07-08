package top.lanscarlos.vulpecula.bacikal.action.canvas.fx

import org.bukkit.entity.Entity
import taboolib.common.util.Vector
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import top.lanscarlos.vulpecula.utils.playerOrNull
import top.lanscarlos.vulpecula.utils.toBukkit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * 实体朝向
 *
 * @author Lanscarlos
 * @since 2023-06-30 01:37
 */
class EntityDirectionFx(val entity: Entity, val normalize: Boolean, val ignoreVertical: Boolean) : VectorFx<Entity>() {

    override fun calculate(): Vector {
        return calculate(entity)
    }

    override fun calculate(input: Entity): Vector {
        val direction = input.location.direction.let {
            Vector(it.x, if (ignoreVertical) 0.0 else it.y, it.z)
        }
        return if (normalize) direction.normalize() else direction
    }

    override fun copy(): Fx<Entity, Vector> {
        // 无需克隆
        return this
    }

    companion object : ActionFx.Resolver {

        override val name = arrayOf("entity-direction", "entity-face")

        override fun resolve(reader: BacikalReader): Bacikal.Parser<Fx<*, *>> {
            return reader.run {
                combine(
                    argument("bind", "entity", then = entity(display = "fx entity-direction entity")),
                    argument("normalize", "normal", then = bool(display = "fx entity-direction normalize"), def = false),
                    argument("igv", then = bool(display = "fx entity-direction igv"), def = false)
                ) { entity, normalize, ignoreVertical ->
                    EntityDirectionFx(
                        entity ?: this.playerOrNull()?.toBukkit() ?: error("No fx entity-direction entity found."),
                        normalize,
                        ignoreVertical
                    )
                }
            }
        }
    }
}