package top.lanscarlos.vulpecula.bacikal.action.canvas.pattern

import taboolib.common.util.Location
import taboolib.common.util.Vector
import top.lanscarlos.vulpecula.bacikal.action.canvas.fx.VectorFx
import top.lanscarlos.vulpecula.bacikal.action.canvas.fx.fxVector
import kotlin.math.absoluteValue

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2023-07-07 13:33
 */
class LinePattern(
    val target: VectorFx<*>,
    val step: Double,
    val animate: String
) : CanvasPattern {

    // 从 -step 开始，因此第一个点是 origin
    var current = -step

    override fun point(origin: Location): Location {
        val direction = target.calculate()

        when (animate) {
            "repeat", "looping" -> {
                // 重复动画
                if (current > direction.length()) {
                    current = -step
                }
            }
            "yoyo" -> {
                // 往返动画
                if (current > direction.length()) {
                    current = -current // 反向
                }
            }
        }

        current += step
        return origin.clone().add(direction.clone().normalize().multiply(current.absoluteValue))
    }

    override fun shape(origin: Location): Collection<Location> {
        return drawLine(origin, target.calculate(), step)
    }

    companion object : ActionPattern.PatternResolver {

        override val name = arrayOf("line")

        override fun resolve(reader: ActionPattern.Reader): ActionPattern.Handler<CanvasPattern> {
            return reader.handle {
                combine(
                    trim("to", then = fxVector()),
                    argument("step", then = double(), def = 0.1),
                    argument("animate", then = text(), def = "repeat")
                ) { target, step, animate ->
                    if (step <= 0) {
                        error("Step must be greater than 0")
                    }
                    LinePattern(target, step, animate.lowercase())
                }
            }
        }

        fun drawLine(origin: Location, direction: Vector, step: Double): Collection<Location> {
            if (step <= 0) {
                error("Step must be greater than 0")
            }

            val length = direction.length()
            val vector = direction.normalize()
            val result = mutableListOf<Location>()

            var current = 0.0
            while (current <= length) {
                result.add(origin.clone().add(vector.clone().multiply(current)))
                current += step
            }
            return result
        }

    }
}