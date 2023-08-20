package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.pattern

import taboolib.common.util.Location
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2023-07-03 22:18
 */
class LissajousCurvePattern(
    val coefficients: List<Triple<Double, Double, Double>>,
    val startAngle: Double,
    val endAngle: Double,
    val step: Double
) : CanvasPattern {

    var currentAngle = startAngle

    override fun point(origin: Location): Location {
        val x = coefficients.sumOf { (a, b, c) -> b * sin(a * currentAngle + c) }
        val z = coefficients.sumOf { (a, b, c) -> b * cos(a * currentAngle + c) }
        currentAngle += step
        return origin.clone().add(x * 25, 0.0, z * 25)
    }

    override fun shape(origin: Location): Collection<Location> {
        val points = mutableListOf<Location>()
        var angle = startAngle
        while (angle < endAngle) {
            val x = coefficients.sumOf { (a, b, c) -> b * sin(a * angle + c) }
            val z = coefficients.sumOf { (a, b, c) -> b * cos(a * angle + c) }
            points += origin.clone().add(x * 25, 0.0, z * 25)
            angle += step
        }
        return points
    }

    companion object : ActionPattern.PatternResolver {

            override val name = arrayOf("Lissajous-Curve", "Lissajous")

            override fun resolve(reader: ActionPattern.Reader): ActionPattern.Handler<CanvasPattern> {
                return reader.handle {
                    combine(
                        list(),
                        argument("start", then = double(), def = 0.0),
                        argument("end", then = double(), def = 360.0),
                        argument("step", "s", then = double(10.0), def = 10.0)
                    ) { coefficients, start, end, step ->
                        LissajousCurvePattern(coefficients.map { line ->
                            if (line.toString().matches("^-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?\$".toRegex())) {
                                val (a, b, c) = line.toString().split(",").map { it.toDouble() * 0.01 }
                                Triple(a, b, c)
                            } else {
                                error("Invalid lissajous coefficient: $line")
                            }
                        }, start, end, step)
                    }
                }
            }
    }
}