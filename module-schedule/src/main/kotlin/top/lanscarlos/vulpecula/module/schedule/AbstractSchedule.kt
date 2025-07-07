package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException
import top.lanscarlos.vulpecula.common.config.*
import top.lanscarlos.vulpecula.module.bacikal.exception.QuestRuntimeException
import top.lanscarlos.vulpecula.module.schedule.exception.TaskNotFoundException
import top.lanscarlos.vulpecula.module.script.Script
import top.lanscarlos.vulpecula.module.script.ScriptService
import top.lanscarlos.vulpecula.common.core.utils.TimeUtil
import top.lanscarlos.vulpecula.common.core.utils.asLang

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
    final override val isAutoStart: Boolean by config.read("auto-start").boolean(false)

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

    val onExecuteScript: Script by config.read("execute").convert(::parseScript)

    val onStartScript: Script? by config.read("on-start").convert(::parseScriptOrNull)

    val onStopScript: Script? by config.read("on-stop").convert(::parseScriptOrNull)

    val onPauseScript: Script? by config.read("on-pause").convert(::parseScriptOrNull)

    val onResumeScript: Script? by config.read("on-resume").convert(::parseScriptOrNull)

    private var currentPid: Long = 0

    init {
        require(!isAutoStart || !prototype) { asLang("module-schedule-exception-conflict-autostart") }
    }

    override fun pause(pid: String) {
        if (pid == "*") {
            tasks.values.forEach(ScheduleTask::stop)
            return
        }
        val task = tasks[pid] ?: throw TaskNotFoundException(pid)
        task.pause()
    }

    override fun resume(pid: String) {
        if (pid == "*") {
            tasks.values.forEach(ScheduleTask::stop)
            return
        }
        val task = tasks[pid] ?: throw TaskNotFoundException(pid)
        task.resume()
    }

    override fun stop(pid: String) {
        if (pid == "*") {
            tasks.values.forEach(ScheduleTask::stop)
            return
        }
        val task = tasks[pid] ?: throw TaskNotFoundException(pid)
        task.stop()
    }

    protected fun parseTime(value: Any?): Long {
        if (value == null) {
            return -1L
        }
        return when (value) {
            is Int -> value.toLong() * 50L
            is Long -> value * 50L
            is String -> TimeUtil.parse(value)
            else -> throw TypeConversionException(value::class.java, Long::class.java)
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
            asLang("module-schedule-exception-invalid-type", value::class.java.name)
        }
        require(value.isNotBlank()) {
            asLang("module-schedule-exception-invalid-blank")
        }
        return ScriptService.compile(value)
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
                    variables = variables
                ).onFailure(::onFailure)
            }
        }

        fun variables(): Map<String, Any> {
            return mapOf<String, Any>(
                "pid" to pid,
                "count" to counter
            )
        }

        fun onFailure(ex: QuestRuntimeException) {
            // 脚本运行异常时, 暂停任务
            pause()
            console().error { asLang("module-schedule-run-failure", id, pid) }
            ex.printLocalizedMessage(console(), ScheduleService.name)
        }

        override fun start() {
            require(activationTime < 0L) {
                asLang("module-schedule-exception-repetition-start", id, pid)
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
            activationTime = now + delay.coerceAtLeast(0)
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