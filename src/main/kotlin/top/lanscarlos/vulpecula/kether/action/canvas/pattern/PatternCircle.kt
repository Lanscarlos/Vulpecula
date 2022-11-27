package top.lanscarlos.vulpecula.kether.action.canvas.pattern

import taboolib.common.util.Location
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2022-11-10 10:33
 */

class PatternCircle : CanvasPattern {

    var radius = 1.0
    var step = 10.0
    var yOffset = 0.0

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

    companion object : CanvasPattern.Reader {

        override val name = arrayOf("Circle")

        override fun read(reader: QuestReader): CanvasPattern.Builder {
            val options = mutableMapOf<String, LiveData<*>>()

            while (reader.nextPeek().startsWith('-')) {
                when (reader.nextToken().substring(1)) {
                    "radius", "r" -> {
                        options["radius"] = reader.readDouble()
                    }
                    "step", "s" -> {
                        options["step"] = reader.readDouble()
                    }
                    "y-offset", "y" -> {
                        options["y-offset"] = reader.readDouble()
                    }
                }
            }

            return buildFuture {
                options.mapValues { it.value.getOrNull(this) }.thenTake().thenApply { args ->
                    val pattern = PatternCircle()
                    for (option in args) {
                        when (option.key) {
                            "radius" -> pattern.radius = option.value?.coerceDouble() ?: continue
                            "step" -> pattern.step = option.value?.coerceDouble() ?: continue
                            "y-offset" -> pattern.yOffset = option.value?.coerceDouble() ?: continue
                        }
                    }
                    return@thenApply pattern
                }
            }
        }
    }
}