package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.boolean
import top.lanscarlos.vulpecula.common.livedata.convert
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * 循环间隔日程
 *
 * @author Lanscarlos
 * @since 2025/5/9 11:50
 */
class PeriodicSchedule(id: String, config: Configuration) : AbstractSchedule(id, config) {

    val period by config.read("period").convert(::parseTime)

    val baseTime: Long by config.read("base-time").convert(::parseBaseTime)

    override val tasks: HashMap<String, Task> = HashMap()

    override fun create(id: String, senderSelector: String, args: List<String>): ScheduleTask {
        TODO("Not yet implemented")
    }

    override fun start(senderSelector: String, args: List<String>): ScheduleTask {
        require(prototype || tasks.all { !it.state.isRunning }) { "非原型模式下, 当前有任务正在运行." }
        val pid = nextPid()
        return start(pid, pid.toString(), senderSelector, args)
    }

    override fun start(id: String, senderSelector: String, args: List<String>): ScheduleTask {
        require(prototype || tasks.all { !it.state.isRunning }) { "非原型模式下, 当前有任务正在运行." }

        if (id != "~") {
            val task = tasks.find { it.id == id }

        }

        val pid = nextPid()
        val newId = if (id == "@") pid.toString() else id
        return start(pid, newId, senderSelector, args)
    }

    private fun start(pid: Long, id: String, senderSelector: String, args: List<String>): ScheduleTask {
        val task = Task(pid, id, senderSelector, args)
        tasks += task.also(Task::start)
        return task
    }

    inner class Task(
        override val pid: Long,
        override val id: String,
        val senderSelector: String,
        override val args: List<String>
    ) : AbstractTask() {

        override val activationTime: Long = System.currentTimeMillis() + delay.coerceAtLeast(0)

        override var expirationTime: Long = if (duration > 0) activationTime + duration else -1L

        override lateinit var controller: PlatformExecutor.PlatformTask

        private fun onTick() {
            if (state == TaskState.WAITING) {
                state = TaskState.RUNNING
            }
            if (!canContinue()) {
                stop()
                return
            }
            runScript(script, senderSelector, args())
        }

        override fun start() {
            require(::controller.isInitialized.not()) { "禁止重复调用 start() 函数." }
            onStart(emptyMap())
            val now = System.currentTimeMillis()
            val nextTime = calculateNextTime(activationTime)
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

        override fun resume() {
            if (state != TaskState.PAUSED) {
                return
            }
            state = TaskState.WAITING
            onResume(emptyMap())
            val now = System.currentTimeMillis()

            // 修正失效时间
            if (expirationTime > 0) {
                val consumedTime = interruptionTime - activationTime
                val remainingTime = duration - consumedTime
                expirationTime = now + remainingTime
            }

            val nextTime = calculateNextTime(now)
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
        require(value is String) { "类型不正确" }
        val time = try {
            LocalTime.parse(value)
        } catch (_: DateTimeParseException) {
            error("格式不正确")
        }
        return LocalDate.now().atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

}