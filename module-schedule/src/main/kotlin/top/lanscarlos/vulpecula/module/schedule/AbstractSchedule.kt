package top.lanscarlos.vulpecula.module.schedule

import org.bukkit.Bukkit
import org.bukkit.util.BoundingBox
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.configuration.Configuration
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.common.applicative.LocationApplicative
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.*
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.schedule.PeriodicSchedule.Task
import top.lanscarlos.vulpecula.module.script.Script
import top.lanscarlos.vulpecula.module.script.ScriptService
import java.util.function.Consumer
import java.util.function.Function
import kotlin.math.pow

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * @author Lanscarlos
 * @since 2025/5/9 11:51
 */
abstract class AbstractSchedule(override val id: String, val config: Configuration) : Schedule {

    val duration: Long by config.read("duration").convert(::parseTime)

    val maxRuns: Int by config.read("max-runs").int(-1)

    val delay by config.read("delay").convert(::parseTime)

    override val isAutoStart: Boolean by config.read("auto-start").boolean(false)

    val prototype: Boolean by config.read("prototype").boolean(false)

    val isAsynchronous: Boolean by config.read("async").boolean(false)

    override val senderSelector: String by config.read("sender").string("@Console")

    val script: Script by config.read("execute").string().convert(ScriptService::compile)

    val onStartScript: Script? by config.read("on-start").convert(::parseScriptOrNull)

    val onStopScript: Script? by config.read("on-stop").convert(::parseScriptOrNull)

    val onPauseScript: Script? by config.read("on-pause").convert(::parseScriptOrNull)

    val onResumeScript: Script? by config.read("on-resume").convert(::parseScriptOrNull)

    abstract val tasks: HashMap<String, out AbstractTask>

    private var currentPid: Long = 0

    override fun stop(pid: Long) {
        if (pid < 0) {
            tasks.values.forEach(ScheduleTask::stop)
            return
        }
        val task = tasks.values.find { it.pid == pid }
            ?: error("找不到对应的任务 PID:$pid")
        task.stop()
    }

    override fun stop(id: String) {
        if (id == "*") {
            tasks.values.forEach(ScheduleTask::stop)
            return
        }
        val task = tasks[id]
            ?: error("找不到对应的任务 ID:$id")
        task.stop()
    }

    protected fun runScript(
        script: Script,
        senderSelector: String,
        args: Map<String, Any>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ) {
        if (senderSelector.first() != '@') {
            // 指定玩家
            val sender = Bukkit.getPlayerExact(senderSelector)?.let(::adaptPlayer)
                ?: error("无法选取脚本执行者 $senderSelector")
            ScriptService.run(script, sender, args, onSuccess = onSuccess, onFailure = onFailure)
            return
        }
        val selector = senderSelector.substring(1).split(' ')
        when (selector.first().lowercase()) {
            "null" -> ScriptService.run(script, null, args, onSuccess = onSuccess, onFailure = onFailure)
            "console" -> ScriptService.run(script, console(), args, onSuccess = onSuccess, onFailure = onFailure)
            "players" -> {
                for (sender in onlinePlayers()) {
                    ScriptService.run(script, sender, args, onSuccess = onSuccess, onFailure = onFailure)
                }
            }
            "world" -> {
                val senders = selector.getOrNull(1)?.let(Bukkit::getWorld)?.players?.map(::adaptPlayer)
                    ?: error("无法解析世界 ${selector.getOrNull(1)}")
                for (sender in senders) {
                    ScriptService.run(script, sender, args, onSuccess = onSuccess, onFailure = onFailure)
                }
            }
            "range" -> {
                val location = selector.getOrNull(1)?.let(LocationApplicative::convertOrNull)?.toBukkitLocation()
                    ?: error("无法解析坐标 ${selector.getOrNull(1)}")
                val world = location.world
                    ?: error("坐标不合法 ${selector.getOrNull(1)}")
                val range = selector.getOrNull(2)?.toDoubleOrNull()?.pow(2)
                    ?: error("无法解析范围 ${selector.getOrNull(2)}")
                info("@Range 距离平方 >> $range")
                val senders = world.players.filter { it.location.distanceSquared(location) <= range }.map(::adaptPlayer)
                for (sender in senders) {
                    ScriptService.run(script, sender, args, onSuccess = onSuccess, onFailure = onFailure)
                }
            }
            "area" -> {
                val loc1 = selector.getOrNull(1)?.let(LocationApplicative::convertOrNull)?.toBukkitLocation()
                    ?: error("无法解析坐标 ${selector.getOrNull(1)}")
                val loc2 = selector.getOrNull(2)?.let(LocationApplicative::convertOrNull)?.toBukkitLocation()
                    ?: error("无法解析坐标 ${selector.getOrNull(2)}")
                val boundingBox = BoundingBox.of(loc1, loc2)
                val world = loc1.world!!
                val senders = world.players.filter { boundingBox.contains(it.location.x, it.location.y, it.location.z) }.map(::adaptPlayer)
                for (sender in senders) {
                    ScriptService.run(script, sender, args, onSuccess = onSuccess, onFailure = onFailure)
                }
            }
            else -> error("Unknown sender: $senderSelector")
        }
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

    abstract inner class AbstractTask(id: String) : ScheduleTask {

        final override val pid: Long = currentPid++

        override val id: String = if (id != "~") id else pid.toString()

        override var state: TaskState = TaskState.WAITING

        override var activationTime: Long = -1L

        override var expirationTime: Long = -1L

        override var counter: Int = 0

        override var isOutOfDuration: Boolean = false

        override var isOutOfMaxRuns: Boolean = false

        protected var interruptionTime: Long = -1

        abstract val args: List<Any>

        abstract val controller: PlatformExecutor.PlatformTask

        fun args(additions: Map<String, Any> = emptyMap()): Map<String, Any> {
            val args = mutableMapOf<String, Any>(
                "count" to counter
            )
            args.putAll(additions)
            return args
        }

        fun onStart() {
            val script = onStartScript ?: return
            runScript(script, senderSelector, args())
        }

        fun onStop() {
            val script = onStopScript ?: return
            runScript(script, senderSelector, args())
        }

        fun onPause() {
            val script = onPauseScript ?: return
            runScript(script, senderSelector, args(), onSuccess = {}, onFailure = ::onFailure)
        }

        fun onResume() {
            val script = onResumeScript ?: return
            runScript(script, senderSelector, args(), onSuccess = {}, onFailure = ::onFailure)
        }

        fun onFailure(ex: BacikalRuntimeException) {
            // 脚本运行异常时, 暂停任务
            ex.printStackTrace()
            pause()
        }

        abstract fun schedule()

        override fun start() {
            require(activationTime < 0L) { "禁止重复调用 start() 函数." }
            onStart()
            activationTime = System.currentTimeMillis() + delay.coerceAtLeast(0)
            expirationTime = if (duration > 0) activationTime + duration else -1L
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
                val remainingTime = duration - consumedTime
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
            if (++counter > maxRuns) {
                // 已达最大执行次数
                isOutOfMaxRuns = true
                return false
            }
            return true
        }

    }

}