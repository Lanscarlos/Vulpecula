package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.boolean
import top.lanscarlos.vulpecula.common.livedata.convert
import top.lanscarlos.vulpecula.common.livedata.int
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

    val maxRetries by config.read("max-retries").int(-1)

    val retryDelay by config.read("retry-delay").convert(::parseTime)

    val prototype: Boolean by config.read("prototype").boolean(false)

    val tasks: LinkedList<Task> = LinkedList()

    override fun activate() {
        if (!prototype && tasks.isNotEmpty()) {
            // 非原型模式, 且任务在进行中
            return
        }
        val task = Task(System.currentTimeMillis())
        task.start()
        tasks.add(task)
    }

    override fun terminate() {
        for (task in tasks.toMutableList()) {
            task.stop()
        }
    }

    inner class Task(val baseTime: Long) : ScheduleTask {

        val activationTime: Long = System.currentTimeMillis() + delay.coerceAtLeast(0)

        val expirationTime: Long = if (duration > 0) activationTime + duration else -1L

        private var counter: Int = 0

        private lateinit var controller: PlatformExecutor.PlatformTask

        fun start() {
            val now = System.currentTimeMillis()
            require(expirationTime !in 1 until now) {
                // 已超时
                "expiration time is $expirationTime"
            }
            val delay = now - nextTime(now)
            controller = submit(
                now = false,
                async = true,
                delay = delay / 50L + 10L,
                period = period / 50L,
            ) {
                if (!canContinue()) {
                    stop()
                    return@submit
                }
                execute()
            }
        }

        fun stop() {
            controller.cancel()
            tasks.remove(this)
        }

        fun canContinue(): Boolean {
            val now = System.currentTimeMillis()
            if (expirationTime in 1 until now) {
                // 任务已结束
                return false
            }
            if (++counter > maxExecutions) {
                // 已达最大执行次数
                return false
            }
            return true
        }

        fun nextTime(now: Long): Long {
            if (now < activationTime) {
                // 还未开始
                return activationTime
            }
            if (period < 1) {
                // 无循环
                return now
            }
            // 获取过去的循环次数 + 1
            val count = (now - baseTime) / period + 1
            // 计算与下一次任务的时间
            return baseTime + count * period
        }

    }

}