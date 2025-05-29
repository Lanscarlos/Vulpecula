package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.message.MessageService
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

    val period by config.read("period").convert { parseTime("period", it) }

    val baseTime: Long by config.read("base-time").convert(::parseBaseTime)

    override val tasks: HashMap<String, Task> = HashMap()

    override fun create(pid: String, sender: ProxyCommandSender?, args: List<String>): ScheduleTask {
        require(prototype || tasks.values.all { !it.state.isRunning }) {
            val runningPid = tasks.values.firstOrNull { it.state.isRunning }
            MessageService.asLang("module-schedule-exception-conflict-prototype", id, runningPid ?: "null")
        }
        require(!tasks.containsKey(pid) || tasks[pid]!!.state.isRunning) {
            MessageService.asLang("module-schedule-exception-conflict-task", id, pid)
        }
        val task = Task(pid, sender, args)
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
        require(value is String) {
            MessageService.asLang("module-schedule-exception-invalid-content", id, "base-time", value::class.java.name)
        }
        val time = try {
            LocalTime.parse(value)
        } catch (_: DateTimeParseException) {
            error(MessageService.asLang("module-schedule-exception-invalid-content", id, "base-time", value))
        }
        return LocalDate.now().atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

}