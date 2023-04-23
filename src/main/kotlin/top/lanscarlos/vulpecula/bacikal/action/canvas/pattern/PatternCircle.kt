package top.lanscarlos.vulpecula.bacikal.action.canvas.pattern

import taboolib.common.util.Location
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2022-11-10 10:33
 */

class PatternCircle(
    val radius: Double = 1.0,
    val step: Double = 10.0,
    val yOffset: Double = 0.0
) : CanvasPattern {

    var currentAngle = 0.0

    override fun points(origin: Location): Collection<Location> {
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

    override fun nextPoint(origin: Location): Location {
        val radians = Math.toRadians(currentAngle)
        val x = radius * cos(radians)
        val z = radius * sin(radians)
        currentAngle += step
        return origin.clone().add(x, yOffset, z)
    }

    companion object : CanvasPattern.Resolver {

        override val name = arrayOf("circle")

        /**
         * pattern circle -radius &radius -step &step -y-offset &yOffset
         * */
        override fun resolve(reader: BacikalReader): Bacikal.Parser<CanvasPattern> {
            return reader.run {
                combine(
                    argument("radius", "r", then = double(1.0), def = 1.0),
                    argument("step", "s", then = double(10.0), def = 10.0),
                    argument("y-offset", "y", then = double(0.0), def = 0.0)
                ) { radius, step, yOffset ->
                    PatternCircle(radius, step, yOffset)
                }
            }
        }
    }
}