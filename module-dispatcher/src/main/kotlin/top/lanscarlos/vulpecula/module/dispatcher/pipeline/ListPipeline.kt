package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.event.Event
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.Context
import top.lanscarlos.vulpecula.module.dispatcher.Pipeline

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * @author Lanscarlos
 * @since 2025/6/12
 */
class ListPipeline(name: String, clazz: Class<*>, config: ConfigurationSection) : Pipeline<Event> {

    override val priority: Int = 0

    val pipelines: List<Pipeline<*>> = PipelineRegistry.getRelatives(name)
        .map {
            it.getDeclaredConstructor(Class::class.java, ConfigurationSection::class.java)
                .newInstance(clazz, config) as Pipeline<*>
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