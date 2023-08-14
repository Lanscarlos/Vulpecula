package top.lanscarlos.vulpecula.bacikal.action.canvas.pattern

import taboolib.common.util.Location
import top.lanscarlos.vulpecula.bacikal.action.canvas.fx.VectorFx
import top.lanscarlos.vulpecula.bacikal.action.canvas.fx.fxVector
import top.lanscarlos.vulpecula.bacikal.action.canvas.fx.number

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2023-06-29 12:47
 */
class ScalingTransformer(
    val vector: VectorFx<*>?,
    val xOffset: Number?,
    val yOffset: Number?,
    val zOffset: Number?
) : Transformer {

    fun preprocessing(): Triple<Double, Double, Double> {
        val dv = vector?.calculate()
        val dx = this.xOffset?.toDouble() ?: dv?.x ?: 1.0
        val dy = this.yOffset?.toDouble() ?: dv?.y ?: 1.0
        val dz = this.zOffset?.toDouble() ?: dv?.z ?: 1.0
        return Triple(dx, dy, dz)
    }

    fun postprocessing(origin: Location, target: Location, fx: Triple<Double, Double, Double>): Location {
        val (dx, dy, dz) = fx
        if (dx == 1.0 && dy == 1.0 && dz == 1.0) return target

        val vector = target.subtract(origin).toVector()
        return Location(
            target.world,
            origin.x + vector.x * dx,
            origin.y + vector.y * dy,
            origin.z + vector.z * dz
        )
    }

    override fun transform(origin: Location, target: Location): Location {
        val fx = preprocessing()
        return postprocessing(origin, target, fx)
    }

    override fun transform(origin: Location, target: Collection<Location>): Collection<Location> {
        val fx = preprocessing()
        return target.map { postprocessing(origin, it, fx) }
    }

    companion object : ActionPattern.TransformResolver {

        override val name = arrayOf("scaling", "scale")

        override fun resolve(reader: ActionPattern.Reader): ActionPattern.Handler<Transformer> {
            return reader.handle {
                combine(
                    optional("to", then = fxVector()),
                    argument("x", then = number()),
                    argument("y", then = number()),
                    argument("z", then = number())
                ) { vector, x, y, z ->
                    ScalingTransformer(vector, x, y, z)
                }
            }
        }
    }
}