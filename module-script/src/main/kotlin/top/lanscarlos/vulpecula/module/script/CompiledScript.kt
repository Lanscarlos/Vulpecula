package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import taboolib.module.configuration.Configuration
import taboolib.module.kether.deepVars
import top.lanscarlos.vulpecula.bacikal.BacikalService
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.*
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025-03-20 15:11
 */
class CompiledScript(override val id: String, val config: Configuration) : Script {

    data class Parameter(val name: String, val applicative: Applicative<Any>, val optional: Boolean)

    override val file: File
        get() = config.file!!

    val namespace: List<String> by config.read("namespace").stringList()

    val parameters: List<Parameter> by config.read("parameters").mapList().convert(::parseParameters)

    val variables: Map<String, String> by config.read("variables").map().mapTo(::parseStringMap)

    val condition: String by config.read("condition").convert(::parseCondition)

    val deny: String by config.read("deny").string("")

    val main: String by config.read("main").string()

    val functions: Map<String, String> by config.read("functions").map().mapTo(::parseStringMap)

    val timeout: Long by config.read("timeout").convert(::parseTimeout)

    val exceptions: Map<String, Quest> by config.read("exceptions").convert(::parseException)

    private lateinit var quest: Quest

    override fun execute(sender: ProxyCommandSender?, args: List<Any?>): ScriptTask {
        val wrappedArgs = mutableMapOf<String, Any>()
        wrappedArgs["args"] = args
        for ((index, arg) in args.withIndex()) {
            wrappedArgs["arg$index"] = arg ?: continue
        }

        // 参数转换
        for ((index, parameter) in parameters.withIndex()) {
            val arg = args.getOrNull(index)
            if (parameter.optional) {
                val value = arg?.let(parameter.applicative::convertOrNull)
                wrappedArgs[parameter.name] = value ?: continue
                continue
            }
            require(arg != null) { "Missing argument ${parameter.name} at index $index when run script \"$id\"." }
            wrappedArgs[parameter.name] = parameter.applicative.convertOrThrow(arg)
        }

        return run(sender, wrappedArgs)
    }

    override fun execute(sender: ProxyCommandSender?, args: Map<String, Any>): ScriptTask {
        // 参数校验
        val wrappedArgs = HashMap(args)
        for ((name, applicative, optional) in parameters) {
            val arg = args[name]
            if (optional) {
                val value = arg?.let(applicative::convertOrNull)
                wrappedArgs[name] = value ?: continue
                continue
            }
            require(arg != null) { "Missing argument $name when run script \"$id\"." }
            wrappedArgs[name] = applicative.convertOrThrow(arg)
        }

        return run(sender, args)
    }

    private fun run(sender: ProxyCommandSender?, args: Map<String, Any>): ScriptTask {
        if (::quest.isInitialized.not()) {
            buildQuest()
        }
        val pid = ScriptService.nextPid()
        val context = BacikalService.executeLater(quest, sender, args)
        var future = context.runActions()
        val startTime = System.currentTimeMillis()

        // 注入超时检测
        if (timeout > 0) {
            future = future.orTimeout(timeout, TimeUnit.MILLISECONDS)
        }

        future = future.exceptionallyCompose { ex ->
            val exceptionName = ex.javaClass.name
            // 匹配异常处理
            val quest = exceptions.entries.find { exceptionName.endsWith(it.key) }?.value ?: throw ex
            // 执行异常处理
            val exContext = BacikalService.executeLater(quest, sender, args.plus(context.rootFrame().deepVars()))
            exContext.runActions()
        }

        return DefaultScriptTask(pid, this, context, future, startTime)
    }

    override fun buildQuest() {
        val builder = StringBuilder()

        // 构建函数头
        builder.append("def main = {").append('\n')

        // 构建自定义参数
        for ((name, value) in variables) {
            builder.append("set $name to $value").append('\n')
        }

        // 构建函数体
        if (condition.isNotBlank()) {
            builder
                .append("if {").append('\n')
                .append(condition).append('\n')
                .append("} then {").append('\n')
            builder.append(main).append('\n')
            if (deny.isNotBlank()) {
                builder.append("} else {").append('\n')
                builder.append(deny).append('\n')
            }
            builder.append("}").append('\n')
        } else {
            builder.append(main).append('\n')
        }

        // 构建函数尾
        builder.append("}").append('\n')

        // 构建其他函数
        for ((key, value) in functions) {
            builder
                .append('\n')
                .append("def ")
                .append(key)
                .append(" = {").append('\n')
                .append(value).append('\n')
                .append("}").append('\n')
        }

        quest = BacikalService.compile(builder.toString(), id, namespace)
    }

    private fun parseParameters(source: List<Map<*, *>>): List<Parameter> {
        val cache = mutableListOf<Parameter>()
        for (map in source) {
            val name = map["name"].toString()
            val applicative: Applicative<Any> = map["type"].toString().lowercase().let(ApplicativeRegistry::getApplicative)
            val optional = map["optional"].applicativeBoolean(false)
            cache += Parameter(name, applicative, optional)
        }
        return cache
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
        return map.mapValues { (key, value) -> BacikalService.compile(value, "$id-exception-$key", namespace) }
    }

}