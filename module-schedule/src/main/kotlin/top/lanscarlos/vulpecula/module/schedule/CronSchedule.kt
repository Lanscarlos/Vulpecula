package top.lanscarlos.vulpecula.module.schedule

import com.ucasoft.kcron.core.common.*
import com.ucasoft.kcron.cron
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.convert

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * @author Lanscarlos
 * @since 2025/5/9 11:51
 */
class CronSchedule(id: String, config: Configuration) : AbstractSchedule(id, config) {

    val seconds: Pair<CronGroups, String> by config.read("seconds").convert(::parseTimeValue)

    val minutes: Pair<CronGroups, String> by config.read("minutes").convert(::parseTimeValue)

    val hours: Pair<CronGroups, String> by config.read("hours").convert(::parseTimeValue)

    val days: Pair<CronGroups, String>? by config.read("days").convert(::parseDays)

    val weeks: Pair<CronGroups, String>? by config.read("weeks").convert(::parseWeeks)

    val months: Pair<CronGroups, String> by config.read("months").convert(::parseTimeValue)

    val years: Pair<CronGroups, String> by config.read("years").convert(::parseTimeValue)

    init {
        cron {
            seconds(TimeGroups.entries.first { it.index == seconds.first.index }, seconds.second)
            minutes(TimeGroups.entries.first { it.index == minutes.first.index }, minutes.second)
            hours(TimeGroups.entries.first { it.index == hours.first.index }, hours.second)
            if (days != null) {
                val group = DayGroups.entries.first { it.index == days!!.first.index }
                days(group, days!!.second)
            }
            if (weeks != null) {
                val group = DayOfWeekGroups.entries.first { it.index == weeks!!.first.index }
                daysOfWeek(group, weeks!!.second)
            }
            months(months.first, months.second)
            years(years.first, years.second)
        }
    }

    override fun activate() {
        TODO("Not yet implemented")
    }

    override fun terminate() {
        TODO("Not yet implemented")
    }

    private fun parseWeeks(value: Any?): Pair<CronGroups, String>? {
        if (value == null) {
            return null
        }
        if (value !is String) {
            return parseTimeValue(value)
        }
        return when {
            value.matches("^\\dL$".toRegex()) -> DayOfWeekGroups.Last to value
            value.matches("^\\d{1,2}(#| on )\\d{1,2}$".toRegex()) -> {
                DayOfWeekGroups.OfMonth to value.replace(" on ", "#")
            }
            value.matches("^\\d{1,2}(-| to )\\d{1,2}$".toRegex()) -> {
                val (from, to) = value.split("-| to ".toRegex()).map(String::toInt)
                DayGroups.Specific to IntRange(from, to).joinToString(",")
            }
            else -> parseTimeString(value)
        }
    }

    private fun parseDays(value: Any?): Pair<CronGroups, String>? {
        if (value == null) {
            return null
        }
        if (value !is String) {
            return parseTimeValue(value)
        }
        return when {
            value == "L" -> DayGroups.LastDay to "L"
            value == "LW" -> DayGroups.LastWeekday to "LW"
            value.matches("^\\d{1,2}W$".toRegex()) -> DayGroups.LastWeekday to value
            value.matches("^\\d{1,2}(-| to )\\d{1,2}$".toRegex()) -> {
                val (from, to) = value.split("-| to ".toRegex()).map(String::toInt)
                DayGroups.Specific to IntRange(from, to).joinToString(",")
            }
            else -> parseTimeString(value)
        }
    }

    private fun parseTimeValue(value: Any?): Pair<CronGroups, String> {
        return when (value) {
            null -> TimeGroups.Any to "*"
            is String -> parseTimeString(value)
            is Number -> TimeGroups.Specific to value.toString()
            is List<*> -> TimeGroups.Specific to value.joinToString(",")
            else -> error("Invalid value $value.")
        }
    }

    private fun parseTimeString(value: String): Pair<CronGroups, String> {
        return when {
            value == "*" -> TimeGroups.Any to "*"
            value.matches("^\\d{1,2}(,( )?\\d{1,2})+$".toRegex()) -> {
                TimeGroups.Specific to value.replace(" ", "")
            }
            value.matches("^\\d{1,2}(-| to )\\d{1,2}$".toRegex()) -> {
                if (value.contains('-')) {
                    TimeGroups.EveryBetween to value
                } else {
                    TimeGroups.EveryBetween to value.replace(" to ", "-")
                }
            }
            value.matches("^\\d{1,2}(/| at )\\d{1,2}$".toRegex()) -> {
                if (value.contains('/')) {
                    TimeGroups.EveryStartingAt to value.trim()
                } else {
                    val starting = value.substringAfter(" at ")
                    val every = value.substringBefore(" at ")
                    TimeGroups.EveryStartingAt to "$starting/$every"
                }
            }
            else -> error("Invalid value $value.")
        }
    }

}