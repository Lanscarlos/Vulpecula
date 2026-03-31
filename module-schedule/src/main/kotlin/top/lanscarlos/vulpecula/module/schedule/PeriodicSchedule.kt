package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.exception.InvalidTimeFormatException
import top.lanscarlos.vulpecula.common.exception.InvalidTypeException
import top.lanscarlos.vulpecula.common.lang.Lang
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * 循环间隔日程
 *
 * @author lanscarlos
 * @since 2025/5/9 11:50
 */
class PeriodicSchedule(id: String, config: Configuration) : AbstractSchedule(id, config) {

    val period by config.read("period").convert(::parseTime)

    val baseTime: Long by config.read("base-time").convert(::parseBaseTime)

    override val tasks: HashMap<String, Task> = HashMap()

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
        args: List<String>
    ) : AbstractTask(pid, sender, args) {

        override lateinit var controller: PlatformExecutor.PlatformTask

        override fun schedule() {
            val now = System.currentTimeMillis()
            val nextTime = if (interruptionTime > 0) {
                // 从暂停中恢复任务
                calculateNextTime(now)
            } else {
                // 初次运行
                calculateNextTime(activationTime)
            }
            val delay = nextTime - now
            controller = submit(
                now = false,
                async = isAsynchronous,
                delay = delay / 50L + 10L,
                period = period / 50L,
            ) {
                onTick()
            }
        }

        private fun onTick() {
            if (state == TaskState.WAITING) {
                state = TaskState.RUNNING
            }
            if (!canContinue()) {
                stop()
                return
            }
            onExecute()
        }

        /**
         * 计算下一次执行任务的时间戳
         * */
        private fun calculateNextTime(now: Long): Long {
            val baseTime = if (baseTime > 0L) baseTime else activationTime
            if (now - baseTime in -1000L..1000L) {
                // 差距在 1 秒之内
                return now
            }
            // 获取过去的循环次数 + 1
            val count = (now - baseTime) / period + 1
            // 计算与下一次任务的时间
            val nextTime = baseTime + count * period
            return nextTime
        }
    }

    private fun parseBaseTime(value: Any?): Long {
        if (value == null) {
            return -1L
        }
        if (value !is String) {
            throw InvalidTypeException(value)
        }
        val time = try {
            LocalTime.parse(value)
        } catch (_: DateTimeParseException) {
            throw InvalidTimeFormatException(value)
        }
        return LocalDate.now().atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

}