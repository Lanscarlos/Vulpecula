package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.*
import top.lanscarlos.vulpecula.common.message.*
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.schedule.exception.FieldNotFoundException
import top.lanscarlos.vulpecula.module.schedule.exception.InvalidFieldException
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
    val maxDuration: Long by config.read("max-duration").convert(::parseTime).exceptionally("max-duration")

    /**
     * 最大运转次数
     * */
    val maxReplication: Int by config.read("max-replication").int(-1)

    /**
     * 运转延迟
     * */
    val delay by config.read("delay").convert(::parseTime).exceptionally("delay")

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
    val selector: SenderSelector by config.read("sender").string("@Console").convert(SenderSelector::parse).exceptionally("sender")

    val onExecuteScript: Script by config.read("execute").convert(::parseScript).exceptionally("execute")

    val onStartScript: Script? by config.read("on-start").convert(::parseScriptOrNull).exceptionally("on-start")

    val onStopScript: Script? by config.read("on-stop").convert(::parseScriptOrNull).exceptionally("on-stop")

    val onPauseScript: Script? by config.read("on-pause").convert(::parseScriptOrNull).exceptionally("on-pause")

    val onResumeScript: Script? by config.read("on-resume").convert(::parseScriptOrNull).exceptionally("on-resume")

    private var currentPid: Long = 0

    override fun pause(pid: String) {
        if (pid == "*") {
            tasks.values.forEach(ScheduleTask::stop)
            return
        }
        val task = tasks[pid] ?: error(MessageService.asLang("module-schedule-exception-task-not-found", pid))
        task.pause()
    }

    override fun resume(pid: String) {
        if (pid == "*") {
            tasks.values.forEach(ScheduleTask::stop)
            return
        }
        val task = tasks[pid] ?: error(MessageService.asLang("module-schedule-exception-task-not-found", pid))
        task.resume()
    }

    override fun stop(pid: String) {
        if (pid == "*") {
            tasks.values.forEach(ScheduleTask::stop)
            return
        }
        val task = tasks[pid] ?: error(MessageService.asLang("module-schedule-exception-task-not-found", pid))
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
                val matches = regex.find(value) ?: error(MessageService.asLang("module-schedule-exception-invalid-time-format", value))
                val time = matches.groupValues[1].toLong()
                when (val unit = matches.groupValues[2].lowercase()) {
                    "ticks", "tick", "t" -> time * 50
                    "seconds", "second", "s" -> time * 1_000
                    "minutes", "minute", "min", "m" -> time * 60_000
                    "hours", "hour", "h" -> time * 3_600_000
                    else -> error(MessageService.asLang("module-schedule-exception-invalid-time-unit", unit))
                }
            }
            else -> error(MessageService.asLang("module-schedule-exception-invalid-type", value::class.java.name))
        }
    }

    private fun parseScript(value: Any?): Script {
        return parseScriptOrNull(value) ?: throw NullPointerException("value is null.")
    }

    private fun parseScriptOrNull(value: Any?): Script? {
        if (value == null) {
            return null
        }
        require(value is String) {
            MessageService.asLang("module-schedule-exception-invalid-type", value::class.java.name)
        }
        require(value.isNotBlank()) {
            MessageService.asLang("module-schedule-exception-invalid-blank")
        }
        return ScriptService.compile(value)
    }

    protected fun <T> LiveData<T>.exceptionally(field: String): LiveData<T> {
        return this.exceptionally { ex ->
            val detail = when (ex) {
                is NullPointerException -> throw FieldNotFoundException(id, field)
                else -> ex.localizedMessage
            }
            throw InvalidFieldException(id, field, detail)
        }
    }

    abstract inner class AbstractTask(
        pid: String,
        val sender: ProxyCommandSender?,
        val args: List<Any>
    ) : ScheduleTask {

        override val id: String = this@AbstractSchedule.id

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
            pause()
            console().error("module-schedule-run-failure", id, pid)
            console().errorLiteral(ex.getActionMessage())
            console().errorLiteral(ex.getReasonMessage())
            console().errorLiteral(ex.getDetailMessage())
        }

        override fun start() {
            require(activationTime < 0L) {
                MessageService.asLang("module-schedule-exception-repetition-start", id, pid)
            }
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
                counter = maxReplication
                return false
            }
            return true
        }

    }

}