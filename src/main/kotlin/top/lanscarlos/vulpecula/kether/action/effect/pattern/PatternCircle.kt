package top.lanscarlos.vulpecula.kether.action.effect.pattern

import taboolib.common.util.Location
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.getValue
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextPeek
import top.lanscarlos.vulpecula.utils.readDouble
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.effect.pattern
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

    class Builder : CanvasPattern.Builder {

        var radius: LiveData<Double>? = null
        var step: LiveData<Double>? = null
        var yOffset: LiveData<Double>? = null

        override fun build(frame: ScriptFrame): CanvasPattern {
            val pattern = PatternCircle()
            pattern.radius = radius.getValue(frame, pattern.radius)
            pattern.step = step.getValue(frame, pattern.step)
            pattern.yOffset = yOffset.getValue(frame, pattern.yOffset)
            return pattern
        }
    }

    companion object : CanvasPattern.Reader {

        override val name = setOf("circle")

        override fun read(reader: QuestReader): Builder {
            val builder = Builder()
            while (reader.nextPeek().startsWith('-')) {
                when (reader.nextToken().substring(1)) {
                    "radius", "r" -> {
                        builder.radius = reader.readDouble()
                    }
                    "step", "s" -> {
                        builder.step = reader.readDouble()
                    }
                    "y-offset", "y" -> {
                        builder.yOffset = reader.readDouble()
                    }
                }
            }
            return builder
        }
    }
}