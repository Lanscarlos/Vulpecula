package top.lanscarlos.vulpecula.module.schedule

import com.ucasoft.kcron.Cron
import com.ucasoft.kcron.core.builders.Builder
import com.ucasoft.kcron.core.common.*
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.convert
import kotlinx.datetime.LocalDateTime
import com.ucasoft.kcron.kotlinx.datetime.CronLocalDateTime
import com.ucasoft.kcron.kotlinx.datetime.CronLocalDateTimeProvider
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
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
        relocate = [ "!kotlin.", "!kotlin2021.", "!com.ucasoft.kcron.", "!com.ucasoft.kcron0230." ],
    ),
    RuntimeDependency(
        value = "!com.ucasoft.kcron:kcron-common-jvm:0.23.0",
        test = "!com.ucasoft.kcron.Cron",
        relocate = [ "!kotlin.", "!kotlin2021.", "!com.ucasoft.kcron.", "!com.ucasoft.kcron0230." ],
    ),
    RuntimeDependency(
        value = "!com.ucasoft.kcron:kcron-core-jvm:0.23.0",
        test = "!com.ucasoft.kcron.core.Cron",
        relocate = [ "!kotlin.", "!kotlin2021.", "!com.ucasoft.kcron.", "!com.ucasoft.kcron0230." ],
    ),
    RuntimeDependency(
        value = "!com.ucasoft.kcron:kcron-kotlinx-datetime-jvm:0.23.0",
        test = "!com.ucasoft.kcron.kotlinx.datetime.CronLocalDateTime",
        relocate = [ "!kotlin.", "!kotlin2021.", "!com.ucasoft.kcron.", "!com.ucasoft.kcron0230." ]
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
        require(days == null || weeks == null) { "不允许同时设置 days 和 weeks." }
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

    override fun create(pid: String, sender: ProxyCommandSender?, args: List<String>): ScheduleTask {
        require(prototype || tasks.values.all { !it.state.isRunning }) { "非原型模式下只允许一个任务运行." }
        require(!tasks.containsKey(id) || tasks[id]!!.state.isRunning) { "任务 $id 正在运行中" }
        val task = Task(id, sender, args)
        tasks[id] = task
        return task
    }

    inner class Task(
        pid: String,
        sender: ProxyCommandSender?,
        args: List<Any>
    ) : AbstractTask(pid, sender, args) {

        override lateinit var controller: PlatformExecutor.PlatformTask

        private fun onTick() {
            if (!canContinue()) {
                stop()
                return
            }
            onExecute()
            schedule() // 继续触发
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
            require(delay >= 0) { "系统异常 delay 小于 0." }
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
            val nextRun = cron.nextRun ?: error("系统异常 cron.nextRun 为空.")
            val time = nextRun.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            return time
        }

        override fun start() {
            require(::controller.isInitialized.not()) { "禁止重复调用 start() 函数." }
            onStart()
            schedule()
        }

        override fun resume() {
            if (state != TaskState.PAUSED) {
                return
            }
            state = TaskState.WAITING
            onResume()
            val now = System.currentTimeMillis()

            // 修正失效时间
            if (expirationTime > 0) {
                val consumedTime = interruptionTime - activationTime
                val remainingTime = duration - consumedTime
                expirationTime = now + remainingTime
            }

            schedule()
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