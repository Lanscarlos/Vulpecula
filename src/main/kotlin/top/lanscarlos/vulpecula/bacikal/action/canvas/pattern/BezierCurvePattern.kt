package top.lanscarlos.vulpecula.bacikal.action.canvas.pattern

import taboolib.common.util.Location
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveLocation

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2022-11-20 17:07
 */
class BezierCurvePattern(
    locations: List<Location>
) : CanvasPattern {

    var step = 0.05
    val points: List<Location>

    var index = 0

    init {
        points = mutableListOf<Location>().also {
            if (locations.size < 2) error("Pattern BezierCurve requires at least two locations.")
            var t = 0.0
            while (t < 1) {
                it.add(calculate(locations, t))
                t += step
            }
        }
    }

    fun calculate(raw: List<Location>, step: Double): Location {
        if (raw.size == 2) {
            return raw[0].clone().add(raw[1].clone().subtract(raw[0]).toVector().multiply(step))
        }

        val temp = ArrayList<Location>()
        for (i in 0 until raw.lastIndex) {

            val p0 = raw[i]
            val p1 = raw[i + 1]

            // 降解处理
            temp.add(p0.clone().add(p1.clone().subtract(p0).toVector().multiply(step)))
        }
        return calculate(temp, step)
    }

    override fun shape(origin: Location): Collection<Location> {
        return points
    }

    override fun point(origin: Location): Location {
        if (index >= points.size) index = 0
        return points[index++]
    }

    companion object : ActionPattern.PatternResolver {

        override val name = arrayOf("Bezier-Curve", "Bezier")

        /**
         * pattern bezier [ &loc1 &loc2... ]
         * */
        override fun resolve(reader: ActionPattern.Reader): ActionPattern.Handler<CanvasPattern> {
            return reader.handle {
                combine(
                    list()
                ) { locations ->
                    BezierCurvePattern(locations.mapNotNull { it?.liveLocation })
                }
            }
        }
    }
}