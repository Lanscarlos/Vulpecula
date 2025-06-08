package top.lanscarlos.vulpecula.common.core.utils

import top.lanscarlos.vulpecula.common.core.exception.InvalidTimeFormatException
import top.lanscarlos.vulpecula.common.core.exception.InvalidTimeUnitException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2025/6/4 9:38
 */
object TimeUtil {

    fun parse(value: String): Long {
        val regex = Regex("^(\\d+)(ticks|tick|t|ms|seconds|second|s|minutes|minute|min|m|hours|hour|h)$", RegexOption.IGNORE_CASE)
        val matches = regex.find(value) ?: throw InvalidTimeFormatException(value)
        val time = matches.groupValues[1].toLong()
        return when (val unit = matches.groupValues[2].lowercase()) {
            "ticks", "tick", "t" -> time * 50
            "ms" -> time
            "seconds", "second", "s" -> time * 1_000
            "minutes", "minute", "min", "m" -> time * 60_000
            "hours", "hour", "h" -> time * 3_600_000
            else -> throw InvalidTimeUnitException(unit)
        }
    }

}