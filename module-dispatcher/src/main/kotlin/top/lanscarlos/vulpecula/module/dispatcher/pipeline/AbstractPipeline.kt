package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.event.Event
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.Pipeline

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * @author Lanscarlos
 * @since 2025/6/12 11:56
 */
abstract class AbstractPipeline<T: Event>(val clazz: Class<*>, val config: ConfigurationSection) : Pipeline {

    override val priority: Int = 8

    @Suppress("UNCHECKED_CAST")
    fun getEvent(context: PipelineContext): T {
        return context.event as T
    }

    override fun initPrincipal(context: PipelineContext) = Unit

    override fun initVariables(context: PipelineContext) = Unit

    override fun filter(context: PipelineContext) = Unit

    override fun afterFilter(context: PipelineContext) = Unit

    override fun postprocess(context: PipelineContext) = Unit

}