package top.lanscarlos.vulpecula.module.schedule

import org.bukkit.Bukkit
import org.bukkit.Location
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
import top.lanscarlos.vulpecula.module.script.Script
import top.lanscarlos.vulpecula.module.script.ScriptService
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

    override val isAutoStart: Boolean by config.read("auto-start").boolean(false)

    val isAsynchronous: Boolean by config.read("async").boolean(false)

    val senderSelector: String by config.read("sender").string("@CONSOLE")

    val script: Script by config.read("execute").string().convert(ScriptService::compile)

    val onStartScript: Script? by config.read("on-start").convert(::parseScriptOrNull)

    val onStopScript: Script? by config.read("on-stop").convert(::parseScriptOrNull)

    val onPauseScript: Script? by config.read("on-pause").convert(::parseScriptOrNull)

    val onResumeScript: Script? by config.read("on-resume").convert(::parseScriptOrNull)

    private fun run(script: Script, senderSelector: String, args: Map<String, Any>) {
        if (senderSelector.first() != '@') {
            // 指定玩家
            val sender = Bukkit.getPlayerExact(senderSelector)?.let(::adaptPlayer)
                ?: error("无法选取脚本执行者 $senderSelector")
            ScriptService.run(script, sender, args, onSuccess = ::onSuccess, onFailure = ::onFailure)
            return
        }
        val selector = senderSelector.substring(1).split(' ')
        when (selector.first().lowercase()) {
            "null" -> ScriptService.run(script, null, args, onSuccess = ::onSuccess, onFailure = ::onFailure)
            "console" -> ScriptService.run(script, console(), args, onSuccess = ::onSuccess, onFailure = ::onFailure)
            "players" -> {
                for (sender in onlinePlayers()) {
                    ScriptService.run(script, sender, args, onSuccess = ::onSuccess, onFailure = ::onFailure)
                }
            }
            "world" -> {
                val senders = selector.getOrNull(1)?.let(Bukkit::getWorld)?.players?.map(::adaptPlayer)
                    ?: error("无法解析世界 ${selector.getOrNull(1)}")
                for (sender in senders) {
                    ScriptService.run(script, sender, args, onSuccess = ::onSuccess, onFailure = ::onFailure)
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
                    ScriptService.run(script, sender, args, onSuccess = ::onSuccess, onFailure = ::onFailure)
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
                    ScriptService.run(script, sender, args, onSuccess = ::onSuccess, onFailure = ::onFailure)
                }
            }
            else -> error("Unknown sender: $senderSelector")
        }
    }

    fun execute(args: Map<String, Any>) {
        run(script, senderSelector, args)
    }

    fun onSuccess(value: Any?) {}

    fun onFailure(ex: BacikalRuntimeException) {}

    fun onStart(args: Map<String, Any>) {
        val script = onStartScript ?: return
        run(script, senderSelector, args)
    }

    fun onStop(args: Map<String, Any>) {
        val script = onStopScript ?: return
        run(script, senderSelector, args)
    }

    fun onPause(args: Map<String, Any>) {
        val script = onPauseScript ?: return
        run(script, senderSelector, args)
    }

    fun onResume(args: Map<String, Any>) {
        val script = onResumeScript ?: return
        run(script, senderSelector, args)
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

    abstract inner class AbstractTask : ScheduleTask {

        override var state: TaskState = TaskState.WAITING

        override var counter: Int = 0

        override var isOutOfDuration: Boolean = false

        override var isOutOfMaxRuns: Boolean = false

        protected var interruptionTime: Long = -1

        abstract val controller: PlatformExecutor.PlatformTask

        override fun pause() {
            if (!state.isRunning) {
                return
            }
            state = TaskState.PAUSED
            interruptionTime = System.currentTimeMillis()
            controller.cancel()
            onPause(emptyMap())
        }

        override fun stop() {
            if (state == TaskState.TERMINATED) {
                return
            }
            state = TaskState.TERMINATED
            controller.cancel()
            onStop(emptyMap())
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