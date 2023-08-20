package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.pattern

import taboolib.common.util.Location
import top.lanscarlos.vulpecula.legacy.bacikal.LiveData.Companion.liveDouble
import kotlin.math.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * 多边形
 *
 * @author Lanscarlos
 * @since 2023-07-10 21:07
 */
class PolygonPattern(
    val points: List<Pair<Double, Double>>, // 顶点集合
    val step: Double, // 步长
    val close: Boolean, // 是否闭合
) : CanvasPattern {

    var index = -1
    var xStep = 0.0
    var zStep = 0.0
    var x = 0.0
    var z = 0.0

    override fun point(origin: Location): Location {

        val completed = points.getOrNull(index)?.let { (tx, tz) ->
            val xCompleted = xStep.absoluteValue > 1e-5 && if (xStep > 0) x > tx else x < tx
            val zCompleted = zStep.absoluteValue > 1e-5 && if (zStep > 0) z > tz else z < tz
            xCompleted || zCompleted
        } ?: true


        if (completed) {
            // 未初始化 或者 当前边已绘制到目标点

            if (index < 0 || (!close && index == points.lastIndex)) {
                // 当前边为最后一条边 且 不允许闭合
                // 修正 index 至第一条边
                index = 0
            }

            val (x1, z1) = points[index]
            val (x2, z2) = points[(index + 1) % points.size]
            val dx = x2 - x1
            val dz = z2 - z1
            val distance = sqrt(dx * dx + dz * dz)
            xStep = dx / distance * step
            zStep = dz / distance * step
            x = x1
            z = z1

            // index 循环自增
            index = (index + 1) % points.size

            return origin.clone().add(x, 0.0, z)
        } else {
            // 当前边未绘制到目标点

            x += xStep
            z += zStep
            return origin.clone().add(x, 0.0, z)
        }
    }

    override fun shape(origin: Location): Collection<Location> {
        return drawPolygon(origin, points, step, close)
    }

    companion object : ActionPattern.PatternResolver {

        override val name = arrayOf("polygon")

        override fun resolve(reader: ActionPattern.Reader): ActionPattern.Handler<CanvasPattern> {
            return reader.handle {
                combine(
                    list(),
                    argument("step", "s", then = double(), def = 0.1),
                    argument("init", "i", then = double(), def = 0.0),
                    argument("close", "c", then = bool(), def = true),
                ) { angles, step, init, close ->
                    val points = angles.map {
                        val angle = it?.liveDouble ?: error("Illegal polygon angle: $it")
                        val radians = Math.toRadians(angle + init)
                        cos(radians) to sin(radians)
                    }

                    PolygonPattern(points, step, close)
                }
            }
        }

        /**
         * 在平面上画一个多边形
         * @param origin 原点
         * @param points 顶点集合
         * @param step 步长
         * @param close 是否闭合
         * */
        fun drawPolygon(
            origin: Location,
            points: List<Pair<Double, Double>>,
            step: Double,
            close: Boolean
        ): Collection<Location> {
            val result = mutableListOf<Location>()
            for (i in points.indices) {
                if (!close && i == points.lastIndex) {
                    // 不闭合时，跳出最后一个点
                    break
                }
                val start = points[i]
                val end = points[(i + 1) % points.size]
                result += drawLine(origin, start, end, step)
            }
            return result
        }

        /**
         * 在平面上画一条线
         * @param origin 原点
         * @param start 起点
         * @param end 终点
         * @param step 步长
         * */
        fun drawLine(
            origin: Location,
            start: Pair<Double, Double>,
            end: Pair<Double, Double>,
            step: Double
        ): Collection<Location> {
            val result = mutableListOf<Location>()
            val (x1, z1) = start
            val (x2, z2) = end
            val dx = x2 - x1
            val dz = z2 - z1
            val length = sqrt(dx * dx + dz * dz)
            val xStep = dx / length * step
            val zStep = dz / length * step
            var x = x1
            var z = z1

            while ((xStep.absoluteValue < 1e-5 || if (xStep > 0) x <= x2 else x >= x2) && (zStep.absoluteValue < 1e-5 || if (zStep > 0) z <= z2 else z >= z2)) {
                result += origin.clone().add(x, 0.0, z)
                x += xStep
                z += zStep
            }
            return result
        }
    }
}