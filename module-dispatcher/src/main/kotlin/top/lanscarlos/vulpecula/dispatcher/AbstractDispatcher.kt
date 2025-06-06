package top.lanscarlos.vulpecula.dispatcher

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common5.Baffle
import taboolib.common5.Baffle.BaffleCounter
import taboolib.common5.Baffle.BaffleTime
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ReflexClass
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.applicative.BooleanApplicative
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.config.int
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.config.string
import top.lanscarlos.vulpecula.common.core.exception.InvalidTypeException
import top.lanscarlos.vulpecula.common.lang.asLang
import top.lanscarlos.vulpecula.common.lang.error
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.script.Script
import top.lanscarlos.vulpecula.module.script.ScriptService
import top.lanscarlos.vulpecula.utils.TimeUtil
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/4 9:23
 */
abstract class AbstractDispatcher(override val id: String, val config: Configuration) : Dispatcher {

    override val clazz: ReflexClass by config.read("listen-event").string().convert(::parseEventClass)

    override val priority: EventPriority by config.read("listen-priority").string("NORMAL").convert(::parseEventPriority)

    override val weight: Int by config.read("weight").int(8)

    override val preprocessing: Script? by config.read("pre-processing").convert(::parseScriptOrNull)

    override val postprocessing: Script? by config.read("post-processing").convert(::parseScriptOrNull)

    override val executable: Script by config.read("execute").convert(::parseScript)

    val rule: Rule<Event> by config.read("rule").convert(::parseRule)

    override fun reload(file: File) {
        config.loadFromFile(file)
    }

    override fun accept(event: Event) {
        val player = rule.parsePlayer(event)
        val context = Context(event, player)

        if (!rule.matches(context)) {
            return
        }

        val sender = context.player?.let(::adaptPlayer) ?: console()
        var variables = rule.parseVariables(event).mapNotNull { (k, v) -> v?.let { k to it } }.toMap()

        // 执行前置处理
        if (preprocessing != null) {
            val task = ScriptService.run(
                script = preprocessing!!,
                sender = sender,
                variables = variables,
                onFailure = ::onFailure
            )
            variables = task.variables()
            when (val status = variables["@EVENT_STATUS"]) {
                null -> {}
                "CANCEL" -> {
                    require(event is Cancellable) { "Event $event is not Cancellable" }
                    event.isCancelled = true
                    return
                }
                "FILTER" -> {
                    return
                }
                else -> error("Unknown event status $status")
            }
        }

        ScriptService.run(
            script = executable,
            sender = context.player?.let(::adaptPlayer) ?: console(),
            variables = variables,
            onFailure = ::onFailure
        )

        // TODO 更新变量
        // TODO 执行后置处理

        // 更新阻断
        rule.updateBaffle(context)
    }

    fun onFailure(ex: BacikalRuntimeException) {
        // 脚本运行异常时, 暂停任务
        console().error { asLang("module-dispatcher-run-failure", id) }
        console().error { ex.getActionMessage() }
        console().error { ex.getReasonMessage() }
        console().error { ex.getDetailMessage() }
    }

    private fun parseRule(value: Any?): Rule<Event> {
        if (value == null) {
            return Rule.of(clazz, Configuration.empty())
        }
        require(value is ConfigurationSection) { "Invalid configuration section: $value" }
        return Rule.of(clazz, value)
    }

    private fun parseScript(value: Any?): Script {
        return parseScriptOrNull(value) ?: throw NullPointerException()
    }

    private fun parseScriptOrNull(value: Any?): Script? {
        if (value == null) {
            return null
        }
        require(value is String) {
            asLang("module-dispatcher-exception-invalid-type", value::class.java.name)
        }
        require(value.isNotBlank()) {
            asLang("module-dispatcher-exception-invalid-blank")
        }
        return ScriptService.compile(value)
    }

    private fun parseEventPriority(value: String): EventPriority {
        val name = value.uppercase()
        require(name in EventPriority.entries.map(EventPriority::name)) { "Priority must be one of ${EventPriority.entries.map(EventPriority::name)}" }
        return EventPriority.valueOf(name)
    }

    private fun parseEventClass(value: String): ReflexClass {
        require(value.isNotBlank()) { "Event class cannot be null or blank." }
        val clazz = try {
            Class.forName(value)
        } catch (e: ClassNotFoundException) {
            error("Event class not found: $value")
        }
        require(Event::class.java.isAssignableFrom(clazz)) { "Event class must be subclass of class ${Event::class.java.name}" }
        return ReflexClass.of(clazz)
    }

}