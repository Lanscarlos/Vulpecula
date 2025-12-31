package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldReadException
import top.lanscarlos.vulpecula.module.dispatcher.Pipeline
import java.lang.reflect.InvocationTargetException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * @author Lanscarlos
 * @since 2025/6/12
 */
class ListPipeline(name: String, clazz: Class<*>, config: ConfigurationSection) : Pipeline {

    override val priority: Int = 0

    val pipelines: List<Pipeline> = initPipelines(name, clazz, config)

    private fun initPipelines(name: String, clazz: Class<*>, config: ConfigurationSection): List<Pipeline> {
        try {
            return PipelineRegistry.getRelatives(name)
                .map {
                    it.getDeclaredConstructor(Class::class.java, ConfigurationSection::class.java)
                        .newInstance(clazz, config) as Pipeline
                }.sortedByDescending {
                    it.priority
                }
        } catch (e: InvocationTargetException) {
            when (val targetException = e.targetException) {
                is ConfigFieldReadException -> throw targetException.cause
                else -> throw e
            }
        }
    }

    override fun initPrincipal(context: PipelineContext) {
        for (pipeline in pipelines) {
            pipeline.initPrincipal(context)
        }
    }

    override fun initVariables(context: PipelineContext) {
        for (pipeline in pipelines) {
            pipeline.initVariables(context)
        }
    }

    override fun filter(context: PipelineContext) {
        for (pipeline in pipelines) {
            pipeline.filter(context)
        }
    }

    override fun afterFilter(context: PipelineContext) {
        for (pipeline in pipelines) {
            pipeline.afterFilter(context)
        }
    }

    override fun postprocess(context: PipelineContext) {
        for (pipeline in pipelines) {
            pipeline.postprocess(context)
        }
    }

}