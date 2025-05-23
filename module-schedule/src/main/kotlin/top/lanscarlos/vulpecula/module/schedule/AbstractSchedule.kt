package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.*
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.script.Script
import top.lanscarlos.vulpecula.module.script.ScriptService

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * @author Lanscarlos
 * @since 2025/5/9 11:51
 */
abstract class AbstractSchedule(override val id: String, val config: Configuration) : Schedule {

    /**
     * 最大运转时间
     * */
    val maxDuration: Long by config.read("max-duration").convert(::parseTime)

    /**
     * 最大运转次数
     * */
    val maxReplication: Int by config.read("max-replication").int(-1)

    /**
     * 运转延迟
     * */
    val delay by config.read("delay").convert(::parseTime)

    /**
     * 是否自启动
     * */
    override val isAutoStart: Boolean by config.read("auto-start").boolean(false)

    /**
     * 是否允许多任务
     * */
    val prototype: Boolean by config.read("prototype").boolean(false)

    /**
     * 是否异步运行
     * */
    val isAsynchronous: Boolean by config.read("async").boolean(false)

    /**
     * 脚本执行者选取
     * */
    val selector: SenderSelector by config.read("sender").string("@Console").convert(SenderSelector::parse)

    val onExecuteScript: Script by config.read("execute").string().convert(ScriptService::compile)

    val onStartScript: Script? by config.read("on-start").convert(::parseScriptOrNull)

    val onStopScript: Script? by config.read("on-stop").convert(::parseScriptOrNull)

    val onPauseScript: Script? by config.read("on-pause").convert(::parseScriptOrNull)

    val onResumeScript: Script? by config.read("on-resume").convert(::parseScriptOrNull)

    private var currentPid: Long = 0

    override fun stop(pid: String) {
        if (id == "*") {
            tasks.values.forEach(ScheduleTask::stop)
            return
        }
        val task = tasks[id] ?: error("找不到对应的任务 ID: $id")
        task.stop()
    }

    protected fun parseTime(value: Any?): Long {
        if (value == null) {
            return -1L
        }
        return when (value) {
            is Int -> value.toLong() * 50L
            is Long -> value * 50L
            is String -> {
                val regex = Regex("^(\\d+)(ticks|tick|t|seconds|second|s|minutes|minute|min|m|hours|hour|h)$", RegexOption.IGNORE_CASE)
                val matches = regex.find(value) ?: error("Unsupported time format: $value")
                val time = matches.groupValues[1].toLong()
                when (val unit = matches.groupValues[2].lowercase()) {
                    "ticks", "tick", "t" -> time * 50
                    "seconds", "second", "s" -> time * 1_000
                    "minutes", "minute", "min", "m" -> time * 60_000
                    "hours", "hour", "h" -> time * 3_600_000
                    else -> error("Invalid time unit: $unit")
                }
            }
            else -> error("Unsupported value type: ${value::class.java.name}")
        }
    }

    private fun parseScriptOrNull(value: Any?): Script? {
        if (value == null) {
            return null
        }
        require(value is String)
        require(value.isNotBlank())
        return ScriptService.compile(value)
    }

    abstract inner class AbstractTask(
        pid: String,
        val sender: ProxyCommandSender?,
        val args: List<Any>
    ) : ScheduleTask {

        override val pid: String = if (pid != "~") pid else (currentPid++).toString()

        override var state: TaskState = TaskState.WAITING

        override var activationTime: Long = -1L

        override var expirationTime: Long = -1L

        override var counter: Int = 0

        override var isOutOfDuration: Boolean = false

        override var isOutOfMaxRuns: Boolean = false

        protected var interruptionTime: Long = -1

        abstract val controller: PlatformExecutor.PlatformTask

        abstract fun schedule()

        fun onStart() {
            val script = onStartScript ?: return
            runScript(script)
        }

        fun onExecute() {
            runScript(onExecuteScript)
        }

        fun onStop() {
            val script = onStopScript ?: return
            runScript(script)
        }

        fun onPause() {
            val script = onPauseScript ?: return
            runScript(script)
        }

        fun onResume() {
            val script = onResumeScript ?: return
            runScript(script)
        }

        fun runScript(script: Script) {
            val variables = variables()
            for (sender in selector.select(sender)) {
                ScriptService.run(
                    script = script,
                    sender = sender,
                    args = args,
                    variables = variables,
                    onFailure = ::onFailure
                )
            }
        }

        fun variables(): Map<String, Any> {
            return mapOf<String, Any>(
                "pid" to pid,
                "count" to counter
            )
        }

        fun onFailure(ex: BacikalRuntimeException) {
            // 脚本运行异常时, 暂停任务
            ex.printStackTrace()
            pause()
        }

        override fun start() {
            require(activationTime < 0L) { "禁止重复调用 start() 函数." }
            onStart()
            activationTime = System.currentTimeMillis() + delay.coerceAtLeast(0)
            expirationTime = if (maxDuration > 0) activationTime + maxDuration else -1L
            schedule()
        }

        override fun pause() {
            if (!state.isRunning) {
                return
            }
            state = TaskState.PAUSED
            interruptionTime = System.currentTimeMillis()
            controller.cancel()
            onPause()
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
                val remainingTime = maxDuration - consumedTime
                expirationTime = now + remainingTime
            }

            schedule()
        }

        override fun stop() {
            if (state == TaskState.TERMINATED) {
                return
            }
            state = TaskState.TERMINATED
            controller.cancel()
            onStop()
        }

        protected fun canContinue(): Boolean {
            val now = System.currentTimeMillis()
            if (expirationTime in 1 until now) {
                // 任务已结束
                isOutOfDuration = true
                return false
            }
            if (++counter > maxReplication) {
                // 已达最大执行次数
                isOutOfMaxRuns = true
                return false
            }
            return true
        }

    }

}