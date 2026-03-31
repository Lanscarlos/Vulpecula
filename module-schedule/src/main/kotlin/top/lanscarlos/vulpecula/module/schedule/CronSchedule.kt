package top.lanscarlos.vulpecula.module.schedule

import com.ucasoft.kcron.Cron
import com.ucasoft.kcron.core.builders.Builder
import com.ucasoft.kcron.core.common.*
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.config.convert
import kotlinx.datetime.LocalDateTime
import com.ucasoft.kcron.kotlinx.datetime.CronLocalDateTime
import com.ucasoft.kcron.kotlinx.datetime.CronLocalDateTimeProvider
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.schedule.exception.InvalidCronException
import java.util.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * @author Lanscarlos
 * @since 2025/5/9 11:51
 */
@RuntimeDependencies(
    RuntimeDependency(
        value = "!com.ucasoft.kcron:kcron-abstractions-jvm:0.23.0",
        test = "!com.ucasoft.kcron.abstractions.CronDateTime",
        relocate = [ "!kotlin.", "!kotlin210.", "!com.ucasoft.kcron.", "!com.ucasoft.kcron0230." ]
    ),
    RuntimeDependency(
        value = "!com.ucasoft.kcron:kcron-common-jvm:0.23.0",
        test = "!com.ucasoft.kcron.Cron",
        relocate = [ "!kotlin.", "!kotlin210.", "!com.ucasoft.kcron.", "!com.ucasoft.kcron0230." ]
    ),
    RuntimeDependency(
        value = "!com.ucasoft.kcron:kcron-core-jvm:0.23.0",
        test = "!com.ucasoft.kcron.core.Cron",
        relocate = [ "!kotlin.", "!kotlin210.", "!com.ucasoft.kcron.", "!com.ucasoft.kcron0230." ]
    ),
    RuntimeDependency(
        value = "!com.ucasoft.kcron:kcron-kotlinx-datetime-jvm:0.23.0",
        test = "!com.ucasoft.kcron.kotlinx.datetime.CronLocalDateTime",
        relocate = [ "!kotlin.", "!kotlin210.", "!com.ucasoft.kcron.", "!com.ucasoft.kcron0230." ]
    )
)
class CronSchedule(id: String, config: Configuration) : AbstractSchedule(id, config) {

    val seconds: Pair<CronGroups, String> by config.read("seconds").convert(::parseTimeValue)

    val minutes: Pair<CronGroups, String> by config.read("minutes").convert(::parseTimeValue)

    val hours: Pair<CronGroups, String> by config.read("hours").convert(::parseTimeValue)

    val days: Pair<CronGroups, String>? by config.read("days").convert(::parseDays)

    val weeks: Pair<CronGroups, String>? by config.read("weeks").convert(::parseWeeks)

    val months: Pair<CronGroups, String> by config.read("months").convert(::parseTimeValue)

    val years: Pair<CronGroups, String> by config.read("years").convert(::parseTimeValue)

    val cron: Builder<LocalDateTime, CronLocalDateTime, CronLocalDateTimeProvider>

    override val tasks: HashMap<String, Task> = HashMap()

    init {
        require(days == null || weeks == null) {
            Lang.MODULE_SCHEDULE_EXCEPTION_CONFLICT_DAYS_WEEKS.asText(console(), id)
        }
        cron = Cron.builder()
            .seconds(TimeGroups.entries.first { it.index == seconds.first.index }, seconds.second)
            .minutes(TimeGroups.entries.first { it.index == minutes.first.index }, minutes.second)
            .hours(TimeGroups.entries.first { it.index == hours.first.index }, hours.second)
            .months(MonthGroups.entries.first { it.index == months.first.index }, months.second)
            .years(YearGroups.entries.first { it.index == years.first.index }, years.second)
        if (days != null) {
            val group = DayGroups.entries.first { it.index == days!!.first.index }
            cron.days(group, days!!.second)
        }
        if (weeks != null) {
            val group = DayOfWeekGroups.entries.first { it.index == weeks!!.first.index }
            cron.daysOfWeek(group, weeks!!.second)
        }
    }

    override fun create(pid: String, args: List<String>): ScheduleTask {
        require(prototype || tasks.values.all { !it.state.isRunning }) {
            val runningPid = tasks.values.firstOrNull { it.state.isRunning }
            Lang.MODULE_SCHEDULE_EXCEPTION_CONFLICT_PROTOTYPE.asText(console(), id, runningPid ?: "null")
        }
        require(!tasks.containsKey(pid) || tasks[pid]!!.state.isRunning) {
            Lang.MODULE_SCHEDULE_EXCEPTION_CONFLICT_TASK.asText(console(), id, pid)
        }
        val task = Task(pid, null, args)
        tasks[task.pid] = task
        return task
    }

    inner class Task(
        pid: String,
        sender: ProxyCommandSender?,
        args: List<Any>
    ) : AbstractTask(pid, sender, args) {

        override lateinit var controller: PlatformExecutor.PlatformTask

        private fun onTick() {
            if (state == TaskState.WAITING) {
                state = TaskState.RUNNING
            }
            if (!canContinue()) {
                stop()
                return
            }
            try {
                onExecute()
                schedule() // 继续触发
            } catch (e: Exception) {
                e.printStackTrace()
                pause()
            }
        }

        override fun schedule() {
            val now = System.currentTimeMillis()
            val delay = calculateNextTime() - now
            if (delay <= 50) {
                // 极小概率出现此情况, 为避免触发脚本, 延迟 50ms 再计算
                submit(delay = 1) {
                    schedule()
                }
                return
            }
            require(delay >= 0) { "SYSTEM ERROR: delay < 0." }
            if (::controller.isInitialized) {
                controller.cancel()
            }
            controller = submit(
                now = false,
                async = isAsynchronous,
                delay = (delay / 50L) + 1L, // 加 50ms
                period = 0L
            ) {
                onTick()
            }
        }

        private fun calculateNextTime(): Long {
            val nextRun = cron.nextRun ?: error("SYSTEM ERROR: property cron.nextRun returns null.")
            val time = nextRun.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            return time
        }
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
            else -> throw InvalidCronException(value)
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
            else -> throw InvalidCronException(value)
        }
    }
}