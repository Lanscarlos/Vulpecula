package top.lanscarlos.vulpecula.common.utils

import taboolib.common5.Coerce
import top.lanscarlos.vulpecula.common.exception.InvalidTimeFormatException
import top.lanscarlos.vulpecula.common.exception.InvalidTimeUnitException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2025/6/4 9:38
 */
object TimeUtil {

    fun startTiming(): Long {
        return System.nanoTime()
    }

    /**
     * 结束计时
     *
     * @return 毫秒数
     * */
    fun stopTiming(startTime: Long): Double {
        return Coerce.format((System.nanoTime() - startTime).div(1000000.0))
    }

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