package top.lanscarlos.vulpecula.bacikal.action.canvas.pattern

import taboolib.common.util.Location
import taboolib.common.util.Vector
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.bacikal.action.canvas.fx.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2023-06-29 12:36
 */
class RotationTransformer(
    val angle: Number,
    val vector: VectorFx<*>?,
    val xOffset: Number?,
    val yOffset: Number?,
    val zOffset: Number?
) : Transformer {

    fun preprocessing(): Pair<Double, Triple<Double, Double, Double>> {
        // 默认是逆时针旋转，所以这里要取反顺时针旋转
        val radians = Math.toRadians(-this.angle.toDouble())

        val dv = vector?.calculate()
        val dx = this.xOffset?.toDouble() ?: dv?.x ?: 0.0
        val dy = this.yOffset?.toDouble() ?: dv?.y ?: 0.0
        val dz = this.zOffset?.toDouble() ?: dv?.z ?: 0.0
        return radians to Triple(dx, dy, dz)
    }

    fun postprocessing(origin: Location, target: Location, fx: Pair<Double, Triple<Double, Double, Double>>): Location {
        val radians = fx.first
        val (dx, dy, dz) = fx.second
        if (dx == 0.0 && dy == 0.0 && dz == 0.0) return target

        val vector = when {
            dy == 0.0 && dz == 0.0 -> {
                // 仅绕 x 轴旋转
                target.subtract(origin).toVector().rotateAroundX(radians)
            }

            dx == 0.0 && dz == 0.0 -> {
                // 仅绕 y 轴旋转
                target.subtract(origin).toVector().rotateAroundY(radians)
            }

            dx == 0.0 && dy == 0.0 -> {
                // 仅绕 z 轴旋转
                target.subtract(origin).toVector().rotateAroundZ(radians)
            }

            else -> target.subtract(origin).toVector().rotateAroundAxis(Vector(dx, dy, dz).normalize(), radians)
        }

        return Location(
            target.world,
            origin.x + vector.x,
            origin.y + vector.y,
            origin.z + vector.z
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

        override val name = arrayOf("rotation", "rotate", "rot", "r")

        override fun resolve(reader: ActionPattern.Reader): ActionPattern.Handler<Transformer> {
            return reader.handle {
                combine(
                    number(),
                    trim("around", "by", then = fxVector()),
                    argument("x", then = number()),
                    argument("y", then = number()),
                    argument("z", then = number())
                ) { angle, vector, x, y, z ->
                    RotationTransformer(angle, vector, x, y, z)
                }
            }
        }
    }
}