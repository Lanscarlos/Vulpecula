package top.lanscarlos.vulpecula.bacikal.action.canvas.pattern

import kotlin.math.cos
import kotlin.math.sin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2023-07-07 13:33
 */
object LinePattern : ActionPattern.PatternResolver {

    override val name = arrayOf("line")

    override fun resolve(reader: ActionPattern.Reader): ActionPattern.Handler<CanvasPattern> {
        return reader.handle {
            combine(
                argument("step", "s", then = double(), def = 0.1),
                argument("init", "i", then = double(), def = 0.0),
                argument("loop", then = bool(), def = false)
            ) { step, init, loop ->
                val points = intArrayOf(0, 180).map {
                    val radians = Math.toRadians(it + init)
                    cos(radians) to sin(radians)
                }

                PolygonPattern(points, step, loop)
            }
        }
    }

}