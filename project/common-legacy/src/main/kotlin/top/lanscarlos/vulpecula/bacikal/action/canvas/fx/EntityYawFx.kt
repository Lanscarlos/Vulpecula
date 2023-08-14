package top.lanscarlos.vulpecula.bacikal.action.canvas.fx

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import top.lanscarlos.vulpecula.utils.playerOrNull
import top.lanscarlos.vulpecula.utils.toBukkit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * @author Lanscarlos
 * @since 2023-07-08 13:31
 */
class EntityYawFx(val entity: Entity) : DecimalFx<Entity>() {

    override fun calculate(): Double {
        return calculate(entity)
    }

    override fun calculate(input: Entity): Double {
        return if (entity is Player) {
            entity.eyeLocation.yaw.toDouble()
        } else {
            entity.location.yaw.toDouble()
        }
    }

    override fun copy(): EntityYawFx {
        return EntityYawFx(entity)
    }

    companion object : ActionFx.Resolver {

        override val name = arrayOf("entity-yaw", "yaw")

        override fun resolve(reader: BacikalReader): Bacikal.Parser<Fx<*, *>> {
            return reader.run {
                combine(
                    argument("bind", "entity", then = entity(display = "fx entity-yaw entity"))
                ) { entity ->
                    EntityYawFx(
                        entity ?: this.playerOrNull()?.toBukkit() ?: error("No fx entity-yaw entity found.")
                    )
                }
            }
        }
    }
}