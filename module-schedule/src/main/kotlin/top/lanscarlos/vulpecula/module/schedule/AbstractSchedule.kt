package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import taboolib.module.configuration.Configuration
import taboolib.platform.util.onlinePlayers
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.boolean
import top.lanscarlos.vulpecula.common.livedata.convert
import top.lanscarlos.vulpecula.common.livedata.int
import top.lanscarlos.vulpecula.common.livedata.string
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
abstract class AbstractSchedule(val id: String, val config: Configuration) : Schedule {

    val duration: Long by config.read("duration").convert(::parseTime)

    val maxExecutions: Int by config.read("max-runs").int(-1)

    val autoStart: Boolean by config.read("auto-start").boolean(false)

    val senderSelector: String by config.read("sender").string("@CONSOLE")

    val script: Script by config.read("execute").string().convert(ScriptService::compile)

    fun execute() {
        val args = getArgs()
        when (senderSelector.lowercase()) {
            "@null" -> {
                ScriptService.run(script, null, args, onSuccess = ::onSuccess, onFailure = ::onFailure)
            }
            "@console" -> {
                ScriptService.run(script, console(), args, onSuccess = ::onSuccess, onFailure = ::onFailure)
            }
            "@players" -> {
                for (sender in onlinePlayers()) {
                    ScriptService.run(script, sender, args, onSuccess = ::onSuccess, onFailure = ::onFailure)
                }
            }
            else -> error("Unknown sender: $senderSelector")
        }
    }

    fun getArgs(): Map<String, Any> {
        val args = mutableMapOf<String, Any>()
        // TODO 加入循环次数，当前时间等等数据
        return args
    }

    fun onSuccess(value: Any?) {}

    fun onFailure(ex: BacikalRuntimeException) {}

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

}