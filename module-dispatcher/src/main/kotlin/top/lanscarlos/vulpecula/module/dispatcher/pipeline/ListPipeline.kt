package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.event.Event
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.Context
import top.lanscarlos.vulpecula.module.dispatcher.EventPipeline

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * @author Lanscarlos
 * @since 2025/6/12
 */
class ListPipeline(name: String, clazz: Class<*>, config: ConfigurationSection) : EventPipeline<Event> {

    override val priority: Int = 0

    val pipelines: List<EventPipeline<*>> = PipelineRegistry.getRelatives(name)
        .map {
            it.getDeclaredConstructor(Class::class.java, ConfigurationSection::class.java)
                .newInstance(clazz, config) as EventPipeline<*>
        }.sortedByDescending {
            it.priority
        }

    override fun process(context: Context) {
        for (pipeline in pipelines) {
            pipeline.process(context)
        }
    }

    override fun preprocess(context: Context) {
        for (pipeline in pipelines) {
            pipeline.preprocess(context)
        }
    }

    override fun postprocess(context: Context) {
        for (pipeline in pipelines) {
            pipeline.postprocess(context)
        }
    }

}