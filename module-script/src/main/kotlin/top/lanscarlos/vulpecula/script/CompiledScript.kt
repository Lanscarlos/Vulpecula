package top.lanscarlos.vulpecula.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.bacikal.BacikalAPI
import top.lanscarlos.vulpecula.common.applicative.MapApplicative
import top.lanscarlos.vulpecula.common.applicative.StringApplicative
import top.lanscarlos.vulpecula.common.applicative.applicativeString
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.script
 *
 * @author Lanscarlos
 * @since 2025-03-20 15:11
 */
class CompiledScript(val id: String, val config: Configuration) : Script {

    val namespace: List<String> by config.read("namespace").stringList()

    val variables: Map<String, String> by config.read("variables").map().mapTo(::parseStringMap)

    val condition: String by config.read("condition").convert(::parseCondition)

    val deny: String by config.read("deny").string("")

    val main: String by config.read("main").string()

    val functions: Map<String, String> by config.read("functions").map().mapTo(::parseStringMap)

    val timeout: Long by config.read("timeout").convert(::parseTimeout)

    val exceptions: Map<String, Quest> by config.read("exceptions").convert(::parseException)

    private lateinit var quest: Quest

    override fun runActions(sender: ProxyCommandSender?, args: Map<String, Any>): CompletableFuture<*> {
        if (::quest.isInitialized.not()) {
            quest = buildQuest()
        }
        var future = BacikalAPI.execute(quest, sender, variables.plus(args))
        if (timeout > 0) {
            future = future.orTimeout(timeout, TimeUnit.MILLISECONDS)
        }
        return future.exceptionallyCompose { ex ->
            val exceptionName = ex.javaClass.name
            // 匹配异常处理
            val quest = exceptions.entries.find { exceptionName.endsWith(it.key) }?.value ?: throw ex
            // 执行异常处理
            BacikalAPI.execute(quest, sender, variables.plus(args))
        }
    }

    private fun buildQuest(): Quest {
        val builder = StringBuilder()

        // 构建函数头
        builder.append("def main = {\n")

        // 构建函数体
        if (condition.isNotBlank()) {
            builder.append("if {\n").append(condition).append("\n} then {\n")
            builder.append(main).append("\n")
            if (deny.isNotBlank()) {
                builder.append("} else {\n")
                builder.append(deny).append("\n")
            }
            builder.append("}\n")
        } else {
            builder.append(main).append("\n")
        }

        // 构建函数尾
        builder.append("}\n")

        // 构建其他函数
        for ((key, value) in functions) {
            builder
                .append("\ndef ")
                .append(key)
                .append(" = {\n")
                .append(value)
                .append("\n}\n")
        }

        return BacikalAPI.compile(builder.toString(), id, namespace)
    }

    private fun parseStringMap(entry: Map.Entry<Any?, Any?>): Pair<String, String> {
        return StringApplicative.convertOrThrow(entry.key) to StringApplicative.convertOrThrow(entry.value)
    }

    private fun parseCondition(value: Any?): String {
        if (value == null) {
            return ""
        }
        return when (value) {
            is String -> value
            is List<*> -> {
                val builder = StringBuilder("all [\n")
                for (item in value) {
                    builder.append(item).append("\n")
                }
                builder.append("]")
                builder.toString()
            }
            else -> error("Unsupported condition type: ${value::class.java.name}")
        }
    }

    private fun parseTimeout(value: Any?): Long {
        if (value == null) {
            return -1L
        }
        return when (value) {
            is Int -> value.toLong() * 50L
            is Long -> value * 50L
            is String -> {
                val regex = Regex("^(\\d+)(ticks|tick|t|ms|s|m|h|d)$", RegexOption.IGNORE_CASE)
                val matches = regex.find(value) ?: error("Unsupported time format: $value")
                val time = matches.groupValues[1].toLong()
                when (val unit = matches.groupValues[2].lowercase()) {
                    "ticks", "tick", "t" -> time * 50L
                    "ms" -> time
                    "s" -> time * 1_000
                    "m" -> time * 60_000
                    "h" -> time * 3_600_000
                    "d" -> time * 86_400_000
                    else -> error("Invalid time unit: $unit")
                }
            }
            else -> error("Unsupported timeout type: ${value::class.java.name}")
        }
    }

    private fun parseException(value: Any?): Map<String, Quest> {
        if (value == null) {
            return emptyMap()
        }
        val map = mutableMapOf<String, String>()

        // 加入默认超时处理
        config.getString("on-timeout")?.let { map["java.util.concurrent.TimeoutException"] = it }

        when (value) {
            is List<*> -> {
                for (item in value.map(MapApplicative::convertOrThrow)) {
                    val exception = item["catch"].applicativeString()
                    val script = item["handle"].applicativeString()
                    map[exception] = script
                }
            }
            is Map<*, *> -> {
                for (entry in value) {
                    val exception = entry.key.applicativeString()
                    val script = entry.value.applicativeString()
                    map[exception] = script
                }
            }
            else -> error("Unsupported exception type: ${value::class.java.name}")
        }
        return map.mapValues { (key, value) -> BacikalAPI.compile(value, "$id-exception-$key", namespace) }
    }

}