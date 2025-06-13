package top.lanscarlos.vulpecula.module.dispatcher

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ReflexClass
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.config.int
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.config.string
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.ListPipeline
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.PipelineRegistry
import top.lanscarlos.vulpecula.module.script.Script
import top.lanscarlos.vulpecula.module.script.ScriptFlow
import top.lanscarlos.vulpecula.module.script.ScriptService
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/4 9:23
 */
class DefaultDispatcher(override val id: String, val config: Configuration) : Dispatcher {

    override val clazz: ReflexClass by config.read("listen-event").string().convert(::parseEventClass)

    override val priority: EventPriority by config.read("listen-priority").string("NORMAL").convert(::parseEventPriority)

    override val weight: Int by config.read("weight").int(8)

    override val preprocessing: Script? by config.read("pre-processing").convert(::parseScriptOrNull)

    override val postprocessing: Script? by config.read("post-processing").convert(::parseScriptOrNull)

    override val executable: Script by config.read("execute").convert(::parseScript)

    val pipeline: Pipeline<*> by config.read("rule").convert(::parsePipeline)

    init {
        enable()
    }

    override fun reload(file: File) {
        // 先注销监听器, 再加载配置, 否则会丢失原有的事件类和事件优先级数据
        disable()
        config.loadFromFile(file)
        enable()
    }

    override fun enable() {
        Listener.register(clazz, this)
    }

    override fun disable() {
        Listener.unregister(clazz, this)
    }

    override fun dispose() {
        disable()
    }

    override fun accept(event: Event) {
        val context = Context(event)
        pipeline.preprocess(context)
        pipeline.process(context)

        // 判断处理状态
        when {
            context.isCancelled -> {
                (event as? Cancellable)?.isCancelled = true
                return
            }
            context.isFiltered -> {
                return
            }
        }

        // 流式执行脚本
        val sender = context.player?.let(::adaptPlayer) ?: console()
        val variables = context.variables
        val flow = ScriptFlow(sender, variables)

        // 执行前置处理
        if (preprocessing != null) {
            flow.add(preprocessing!!)
            flow.postprocess { task ->
                when (val status = task.variables()["@EVENT_STATUS"]) {
                    null -> {
                        // 更新阻断
                        pipeline.postprocess(context)
                    }
                    "CANCEL" -> {
                        info("取消事件.")
                        require(event is Cancellable) { "Event $event is not Cancellable" }
                        event.isCancelled = true
                        flow.terminate()
                    }
                    "FILTER" -> {
                        info("过滤事件.")
                        flow.terminate()
                    }
                    else -> error("Unknown event status $status")
                }
            }
        } else {
            // 更新阻断
            pipeline.postprocess(context)
        }

        flow.add(executable)
        if (postprocessing != null) {
            flow.add(postprocessing!!)
        }

        // 处理异常
        flow.onFailure(::onScriptFailure)

        // 执行脚本流
        flow.execute().exceptionally {
            onFailure(it.cause as Exception)
        }
    }

    private fun onFailure(ex: Exception) {
        ex.printStackTrace()
    }

    private fun onScriptFailure(ex: BacikalRuntimeException) {
        // 脚本运行异常时, 暂停任务
        console().error { asLang("module-dispatcher-run-failure", id) }
        console().error { ex.getActionMessage() }
        console().error { ex.getReasonMessage() }
        console().error { ex.getDetailMessage() }
    }

    private fun parsePipeline(value: Any?): Pipeline<*> {
        val name = config.getString("listen-event")!!
        val config = if (value != null) {
            value as ConfigurationSection
        } else {
            Configuration.empty()
        }
        return ListPipeline(name, clazz.toClass(), config)
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
        return ReflexClass.of(PipelineRegistry.mapping(value))
    }

}