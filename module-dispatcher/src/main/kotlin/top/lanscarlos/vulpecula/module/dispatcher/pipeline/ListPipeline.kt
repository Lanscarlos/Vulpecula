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

    override fun initPlayer(context: Context) {
        for (pipeline in pipelines) {
            pipeline.initPlayer(context)
        }
    }

    override fun initVariables(context: Context) {
        for (pipeline in pipelines) {
            pipeline.initVariables(context)
        }
    }

    override fun filter(context: Context) {
        for (pipeline in pipelines) {
            pipeline.filter(context)
        }
    }

    override fun postprocess(context: Context) {
        for (pipeline in pipelines) {
            pipeline.postprocess(context)
        }
    }

}