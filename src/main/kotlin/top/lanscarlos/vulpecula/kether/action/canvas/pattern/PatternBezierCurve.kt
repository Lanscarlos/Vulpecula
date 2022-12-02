package top.lanscarlos.vulpecula.kether.action.canvas.pattern

import taboolib.common.util.Location
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.kether.live.readLocation
import top.lanscarlos.vulpecula.utils.thenTake

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2022-11-20 17:07
 */
class PatternBezierCurve(
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

    override fun points(origin: Location): Collection<Location> {
        return points
    }

    override fun nextPoint(origin: Location): Location {
        if (index >= points.size) index = 0
        return points[index++]
    }

    companion object : CanvasPattern.Reader {

        override val name = arrayOf("Bezier-Curve", "Bezier", "Curve")

        override fun read(reader: QuestReader): CanvasPattern.Builder {
            val locations = mutableListOf<LiveData<Location>>()

            if (reader.hasNextToken("[")) {
                while (!reader.hasNextToken("]")) {
                    locations += reader.readLocation()
                }
            } else {
                locations += reader.readLocation()
            }

            return buildFuture {
                locations.map { it.getOrNull(this) }.thenTake().thenApply { args ->
                    PatternBezierCurve(args.filterIsInstance<Location>())
                }
            }
        }

    }
}