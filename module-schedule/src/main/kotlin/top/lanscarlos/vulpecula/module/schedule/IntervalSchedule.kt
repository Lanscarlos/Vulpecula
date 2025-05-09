package top.lanscarlos.vulpecula.module.schedule

import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.convert

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * 循环间隔日程
 *
 * @author Lanscarlos
 * @since 2025/5/9 11:50
 */
class IntervalSchedule(id: String, config: Configuration) : AbstractSchedule(id, config) {

    val period by config.read("period").convert(::parseTime)

    val delay by config.read("delay").convert(::parseTime)

    val duration by config.read("duration").convert(::parseDuration)

    private fun parseDuration(value: Any?): LongRange {

    }

    private fun parseTime(value: Any?): Long {
        if (value == null) {
            return -1L
        }
        return when (value) {
            is Int -> value.toLong() * 50L
            is Long -> value * 50L
            is String -> {
                val regex = Regex("^(\\d+)(ticks|tick|t|seconds|second|s|minutes|minute|min|m|hours|hour|h)$", RegexOption.IGNORE_CASE)
                val matches = regex.find(value) ?: error("Unsupported time format: $value")
                val time = matches.groupValues[1].toLong()
                when (val unit = matches.groupValues[2].lowercase()) {
                    "ticks", "tick", "t" -> time * 50
                    "seconds", "second", "s" -> time * 1_000
                    "minutes", "minute", "min", "m" -> time * 60_000
                    "hours", "hour", "h" -> time * 3_600_000
                    else -> error("Invalid time unit: $unit")
                }
            }
            else -> error("Unsupported value type: ${value::class.java.name}")
        }
    }

}