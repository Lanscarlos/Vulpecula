package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.fx

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import top.lanscarlos.vulpecula.legacy.bacikal.Bacikal
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalReader
import top.lanscarlos.vulpecula.legacy.utils.playerOrNull
import top.lanscarlos.vulpecula.legacy.utils.toBukkit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * @author Lanscarlos
 * @since 2023-07-08 13:31
 */
class EntityPitchFx(val entity: Entity) : DecimalFx<Entity>() {

    override fun calculate(): Double {
        return calculate(entity)
    }

    override fun calculate(input: Entity): Double {
        return if (entity is Player) {
            entity.eyeLocation.pitch.toDouble()
        } else {
            entity.location.pitch.toDouble()
        }
    }

    override fun copy(): EntityPitchFx {
        return EntityPitchFx(entity)
    }

    companion object : ActionFx.Resolver {

        override val name = arrayOf("entity-pitch", "pitch")

        override fun resolve(reader: BacikalReader): Bacikal.Parser<Fx<*, *>> {
            return reader.run {
                combine(
                    argument("bind", "entity", then = entity(display = "fx entity-pitch entity"))
                ) { entity ->
                    EntityPitchFx(
                        entity ?: this.playerOrNull()?.toBukkit() ?: error("No fx entity-pitch entity found.")
                    )
                }
            }
        }
    }
}