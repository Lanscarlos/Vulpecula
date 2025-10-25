package top.lanscarlos.vulpecula.module.dispatcher.pipeline.entity

import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.PipelineContext
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AutoRegistered

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.entity
 *
 * @author Lanscarlos
 * @since 2025/7/29
 */
@AutoRegistered
class EntityDamageByEntityEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<EntityDamageByEntityEvent>(clazz, config) {

    override fun initVariables(context: PipelineContext) {
        val event = getEvent(context)
        context.setVariable("event.damager", event.damager)
    }

}