package top.lanscarlos.vulpecula.bacikal.action.canvas.pattern

import kotlin.math.cos
import kotlin.math.sin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * 正方形
 *
 * @author Lanscarlos
 * @since 2023-07-10 23:35
 */
object SquarePattern : ActionPattern.PatternResolver {

        override val name = arrayOf("square")

        override fun resolve(reader: ActionPattern.Reader): ActionPattern.Handler<CanvasPattern> {
            return reader.handle {
                combine(
                    argument("step", "s", then = double(0.1), def = 0.1),
                    argument("init", "i", then = double(0.0), def = 0.0)
                ) { step, init ->
                    val points = intArrayOf(45, 135, 225, 315).map {
                        val radians = Math.toRadians(init + it)
                        cos(radians) to sin(radians)
                    }

                    PolygonPattern(points, step, true)
                }
            }
        }
}