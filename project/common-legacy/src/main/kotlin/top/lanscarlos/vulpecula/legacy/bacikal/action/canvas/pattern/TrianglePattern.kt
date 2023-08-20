package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.pattern

import kotlin.math.cos
import kotlin.math.sin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * 三角形
 *
 * @author Lanscarlos
 * @since 2023-07-08 23:46
 */
object TrianglePattern : ActionPattern.PatternResolver {

        override val name = arrayOf("triangle")

        override fun resolve(reader: ActionPattern.Reader): ActionPattern.Handler<CanvasPattern> {
            return reader.handle {
                combine(
                    argument("step", "s", then = double(0.1), def = 0.1),
                    argument("init", "i", then = double(0.0), def = 0.0)
                ) { step, init ->
                    val points = intArrayOf(0, 120, 240).map {
                        val radians = Math.toRadians(it + init)
                        cos(radians) to sin(radians)
                    }

                    PolygonPattern(points, step, true)
                }
            }
        }

}