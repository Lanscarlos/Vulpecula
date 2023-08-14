package top.lanscarlos.vulpecula.bacikal.action.canvas.pattern

import taboolib.common.util.Location
import top.lanscarlos.vulpecula.bacikal.action.canvas.fx.VectorFx
import top.lanscarlos.vulpecula.bacikal.action.canvas.fx.fxVector
import top.lanscarlos.vulpecula.bacikal.action.canvas.fx.number

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * 平移变换
 *
 * @author Lanscarlos
 * @since 2023-06-29 11:48
 */
class TranslationTransformer(
    val vector: VectorFx<*>?,
    val xOffset: Number?,
    val yOffset: Number?,
    val zOffset: Number?,
    val offset: Boolean
) : Transformer {

    fun preprocessing(): Triple<Double, Double, Double> {
        val dv = vector?.calculate()
        val dx = this.xOffset?.toDouble() ?: dv?.x ?: 0.0
        val dy = this.yOffset?.toDouble() ?: dv?.y ?: 0.0
        val dz = this.zOffset?.toDouble() ?: dv?.z ?: 0.0
        return Triple(dx, dy, dz)
    }

    fun postprocessing(origin: Location, target: Location, fx: Triple<Double, Double, Double>): Location {
        val (dx, dy, dz) = fx
        if (dx == 0.0 && dy == 0.0 && dz == 0.0) return target

        return Location(target.world, target.x + dx, target.y + dy, target.z + dz)
    }

    override fun transform(origin: Location, target: Location): Location {
        val fx = preprocessing()

        if (offset) {
            // 修改原点
            origin.x += fx.first
            origin.y += fx.second
            origin.z += fx.third
        }

        return postprocessing(origin, target, fx)
    }

    override fun transform(origin: Location, target: Collection<Location>): Collection<Location> {
        val fx = preprocessing()

        if (offset) {
            // 修改原点
            origin.x += fx.first
            origin.y += fx.second
            origin.z += fx.third
        }

        return target.map { postprocessing(origin, it, fx) }
    }

    companion object : ActionPattern.TransformResolver {

        override val name = arrayOf("translation", "offset")

        override fun resolve(reader: ActionPattern.Reader): ActionPattern.Handler<Transformer> {
            return reader.handle {
                combine(
                    optional("to", then = fxVector()),
                    argument("x", then = number()),
                    argument("y", then = number()),
                    argument("z", then = number())
                ) { vector, x, y, z ->
                    TranslationTransformer(vector, x, y, z, offset = reader.token == "offset")
                }
            }
        }
    }
}