package top.lanscarlos.vulpecula.module.dispatcher.pipeline.entity

import org.bukkit.event.entity.EntityEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.PipelineContext
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AutoRegistered

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.entity
 *
 * @author Lanscarlos
 * @since 2025/6/14 9:24
 */
@AutoRegistered
class EntityEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<EntityEvent>(clazz, config) {

    override fun initPrincipal(context: PipelineContext) {
        context.setPrincipal(getEvent(context).entity)
    }

    override fun initVariables(context: PipelineContext) {
        val event = getEvent(context)
        context.setVariable("event.entity", event.entity)
    }

}