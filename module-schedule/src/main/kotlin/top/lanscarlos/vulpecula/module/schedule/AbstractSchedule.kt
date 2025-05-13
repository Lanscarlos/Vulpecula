package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
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

    abstract inner class AbstractTask : ScheduleTask {}

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

    private fun run(script: Script, args: Map<String, Any>) {
        when (senderSelector.lowercase()) {
            "null" -> ScriptService.run(script, null, args, onSuccess = ::onSuccess, onFailure = ::onFailure)
            "console" -> ScriptService.run(script, console(), args, onSuccess = ::onSuccess, onFailure = ::onFailure)
            "players" -> {
                for (sender in onlinePlayers()) {
                    ScriptService.run(script, sender, args, onSuccess = ::onSuccess, onFailure = ::onFailure)
                }
            }
            else -> error("Unknown sender: $senderSelector")
        }
    }

    fun execute(args: Map<String, Any>) {
        run(script, args)
    }

    fun onSuccess(value: Any?) {}

    fun onFailure(ex: BacikalRuntimeException) {}

    fun onStart(args: Map<String, Any>) {
        val script = onStartScript ?: return
        run(script, args)
    }

    fun onStop(args: Map<String, Any>) {
        val script = onStopScript ?: return
        run(script, args)
    }

    fun onPause(args: Map<String, Any>) {
        val script = onPauseScript ?: return
        run(script, args)
    }

    fun onResume(args: Map<String, Any>) {
        val script = onResumeScript ?: return
        run(script, args)
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

}