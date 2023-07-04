package top.lanscarlos.vulpecula.bacikal.action.canvas.pattern

import taboolib.common.util.Location
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2022-11-10 10:33
 */

class CirclePattern(
    val radius: Double,
    val step: Double,
    val initialAngle: Double
) : CanvasPattern {

    var currentAngle = initialAngle

    override fun point(origin: Location): Location {
        val radians = Math.toRadians(currentAngle)
        val x = radius * cos(radians)
        val z = radius * sin(radians)
        currentAngle += step
        return origin.clone().add(x, 0.0, z)
    }

    override fun shape(origin: Location): Collection<Location> {
        val points = mutableListOf<Location>()
        var angle = 0.0
        while (angle < 360) {
            val radians = Math.toRadians(angle)
            val x = radius * cos(radians)
            val z = radius * sin(radians)
            points += origin.clone().add(x, 0.0, z)
            angle += step
        }
        return points
    }

    companion object : ActionPattern.PatternResolver {

        override val name = arrayOf("circle")

        /**
         * pattern circle -radius ... -step ... -init ...
         * */
        override fun resolve(
            reader: ActionPattern.Reader
        ): ActionPattern.Handler<CanvasPattern> {
            return reader.handle {
                combine(
                    argument("radius", "r", then = double(1.0), def = 1.0),
                    argument("step", "s", then = double(10.0), def = 10.0),
                    argument("init", "i", then = double(0.0), def = 0.0)
                ) { radius, step, init ->
                    CirclePattern(radius, step, init)
                }
            }
        }
    }
}