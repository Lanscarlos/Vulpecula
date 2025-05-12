package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.boolean
import top.lanscarlos.vulpecula.common.livedata.convert
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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
class IntervalSchedule(id: String, config: Configuration) : AbstractSchedule(id, config) {

    val period by config.read("period").convert(::parseTime)

    val delay by config.read("delay").convert(::parseTime)

    val baseTime: Long by config.read("base-time").convert(::parseBaseTime)

    val prototype: Boolean by config.read("prototype").boolean(false)

    val tasks: LinkedList<Task> = LinkedList()

    private var currentPid: Int = 0

    override fun activate() {
        tasks += Task(currentPid++).also(Task::start)
    }

    override fun terminate() {
        for (task in tasks.toMutableList()) {
            task.stop()
        }
    }

    inner class Task(override val pid: Int) : ScheduleTask {

        override var state: TaskState = TaskState.WAITING

        override val activationTime: Long = System.currentTimeMillis() + delay.coerceAtLeast(0)

        override var expirationTime: Long = if (duration > 0) activationTime + duration else -1L

        var interruptionTime: Long = -1

        override var counter: Int = 0

        override var isOutOfDuration: Boolean = false

        override var isOutOfMaxRuns: Boolean = false

        private lateinit var controller: PlatformExecutor.PlatformTask

        private fun onTick() {
            if (state == TaskState.WAITING) {
                state = TaskState.RUNNING
            }
            if (!canContinue()) {
                stop()
                return
            }
            val args = mutableMapOf(
                "count" to counter,
            )
            execute(args)
        }

        override fun start() {
            require(::controller.isInitialized.not()) { "禁止重复调用 start() 函数." }
            val now = System.currentTimeMillis()
            require(expirationTime !in 1 until now) {
                // 已超时
                "expiration time is $expirationTime"
            }
            onStart(emptyMap())
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

        override fun pause() {
            state = TaskState.PAUSED
            interruptionTime = System.currentTimeMillis()
            controller.cancel()
            onPause(emptyMap())
        }

        override fun resume() {
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

        override fun stop() {
            state = TaskState.TERMINATED
            controller.cancel()
            onStop(emptyMap())
        }

        private fun canContinue(): Boolean {
            val now = System.currentTimeMillis()
            if (expirationTime in 1 until now) {
                // 任务已结束
                isOutOfDuration = true
                return false
            }
            if (++counter > maxRuns) {
                // 已达最大执行次数
                isOutOfMaxRuns = true
                return false
            }
            return true
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