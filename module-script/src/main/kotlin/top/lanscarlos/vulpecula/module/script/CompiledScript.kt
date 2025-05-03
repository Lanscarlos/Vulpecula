package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import taboolib.module.configuration.Configuration
import taboolib.module.kether.deepVars
import top.lanscarlos.vulpecula.bacikal.BacikalService
import top.lanscarlos.vulpecula.bacikal.quest.BacikalRuntimeException
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.*
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025-03-20 15:11
 */
class CompiledScript(override val id: String, val config: Configuration) : Script {

    data class Parameter(val name: String, val applicative: Applicative<Any>, val optional: Boolean)

    val namespace: List<String> by config.read("namespace").stringList(emptyList())

    val parameters: List<Parameter> by config.read("parameters").mapList().convert(::parseParameters)

    val variables: Map<String, String> by config.read("variables").map(emptyMap<Any?, Any?>()).mapTo(::parseStringMap)

    val condition: String by config.read("condition").convert(::parseCondition)

    val deny: String by config.read("deny").string("")

    val main: String by config.read("main").string()

    val functions: Map<String, String> by config.read("functions").map(emptyMap<Any?, Any?>()).mapTo(::parseStringMap)

    val timeout: Long by config.read("timeout").convert(::parseTimeout)

    val exceptions: Map<String, Quest> by config.read("exceptions").convert(::parseException)

    private var quest: Quest = buildQuest()

    fun rebuild() {
        quest = buildQuest()
    }

    override fun execute(
        sender: ProxyCommandSender?,
        args: List<Any?>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): ScriptTask {
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

        return run(sender, wrappedArgs, onSuccess, onFailure)
    }

    override fun execute(
        sender: ProxyCommandSender?,
        args: Map<String, Any>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): ScriptTask {
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

        return run(sender, args, onSuccess, onFailure)
    }

    private fun run(
        sender: ProxyCommandSender?,
        args: Map<String, Any>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): ScriptTask {
        val pid = ScriptService.nextPid()
        val context = BacikalService.executeLater(quest, timeout, sender, args)
        val startTime = System.currentTimeMillis()
        val future: CompletableFuture<Any?> = context.runActions().exceptionallyCompose { e ->
            val ex = e.cause as BacikalRuntimeException
            val exceptionName = ex.cause.javaClass.name
            // 匹配异常处理
            val quest = exceptions.entries.find { exceptionName.endsWith(it.key) }?.value
            if (quest == null) {
                // 无异常处理
                throw ex
            }
            // 执行异常处理
            val exContext = BacikalService.executeLater(quest, timeout, sender, args.plus(context.rootFrame().deepVars()))
            exContext.runActions()
        }.handle { result, e ->
            ScriptService.clearTask(pid)
            if (e == null) {
                onSuccess.accept(result)
                return@handle result
            }
            val ex = e.cause as BacikalRuntimeException
            return@handle onFailure.apply(ex).also { ex.printKetherMessage() }
        }

        return DefaultScriptTask(pid, this, context, future, startTime).also(ScriptService::trackTask)
    }

    private fun buildQuest(): Quest {
        val builder = StringBuilder()

        // 构建函数头
        builder.append("def main = {").append('\n')

        // 构建自定义参数
        for ((name, value) in variables) {
            builder.appendIndent("set $name to $value", 1).append('\n')
        }

        // 构建函数体
        if (condition.isNotBlank()) {
            builder
                .appendIndent("if {", 1).append('\n')
                .appendIndent(condition, 2).append('\n')
                .appendIndent("} then {", 1).append('\n')
            builder.appendIndent(main, 2).append('\n')
            if (deny.isNotBlank()) {
                builder.appendIndent("} else {", 1).append('\n')
                builder.appendIndent(deny, 2).append('\n')
            }
            builder.appendIndent("}", 1).append('\n')
        } else {
            builder.appendIndent(main, 1).append('\n')
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
                .appendIndent(value, 1).append('\n')
                .append("}").append('\n')
        }

        // 调试输出
        File(config.file!!.parent, "#${config.file!!.nameWithoutExtension}.ks").writeText(builder.toString())

        return BacikalService.compile(builder.toString(), id, namespace)
    }

    private fun parseParameters(source: List<Map<*, *>>): List<Parameter> {
        val cache = mutableListOf<Parameter>()
        var optional = false
        for (map in source) {
            val name = map["name"].toString()
            val applicative: Applicative<Any> = map["type"].toString().lowercase().let(ApplicativeRegistry::getApplicative)
            optional = optional || map["optional"].applicativeBoolean(false)
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

    private fun StringBuilder.appendIndent(value: String, indent: Int): StringBuilder {
        val lines = value.trim().split('\n')
        val space = "    ".repeat(indent)
        for ((index, line) in lines.withIndex()) {
            append(space)
            append(line)
            if (index != lines.lastIndex) {
                append('\n')
            }
        }
        return this
    }

}