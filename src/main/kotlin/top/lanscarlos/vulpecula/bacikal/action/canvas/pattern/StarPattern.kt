package top.lanscarlos.vulpecula.bacikal.action.canvas.pattern

import taboolib.common.util.Location
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2023-07-11 00:06
 */
class StarPattern(
    val radius: Double,
    val step: Double,
    val initialAngle: Double
) : CanvasPattern {

    var currentAngle = initialAngle

    override fun point(origin: Location): Location {
        val radians = Math.toRadians(currentAngle)
        val x = radius * cos(radians).pow(3.0)
        val z = radius * sin(radians).pow(3.0)
        currentAngle += step
        return origin.clone().add(x, 0.0, z)
    }

    override fun shape(origin: Location): Collection<Location> {
        val points = mutableListOf<Location>()
        var angle = 0.0
        while (angle < 360) {
            val radians = Math.toRadians(angle)
            val x = radius * cos(radians).pow(3.0)
            val z = radius * sin(radians).pow(3.0)
            points += origin.clone().add(x, 0.0, z)
            angle += step
        }
        return points
    }

    companion object : ActionPattern.PatternResolver {

        override val name = arrayOf("star")

        override fun resolve(
            reader: ActionPattern.Reader
        ): ActionPattern.Handler<CanvasPattern> {
            return reader.handle {
                combine(
                    argument("radius", "r", then = double(), def = 1.0),
                    argument("step", "s", then = double(), def = 10.0),
                    argument("init", "i", then = double(), def = 0.0)
                ) { radius, step, init ->
                    StarPattern(radius, step, init)
                }
            }
        }
    }
}