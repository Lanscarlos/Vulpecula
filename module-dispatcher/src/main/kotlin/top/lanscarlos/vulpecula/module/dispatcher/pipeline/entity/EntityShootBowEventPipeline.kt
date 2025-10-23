package top.lanscarlos.vulpecula.module.dispatcher.pipeline.entity

import org.bukkit.event.entity.EntityShootBowEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.PipelineContext
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AutoRegistered

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.entity
 *
 * @author Lanscarlos
 * @since 2025/6/12 9:01
 */
@AutoRegistered
class EntityShootBowEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<EntityShootBowEvent>(clazz, config) {

    override fun initVariables(context: PipelineContext) {
        val event = getEvent(context)
        context.setVariable("projectile", event.projectile)
        context.setVariable("bow", event.bow)
        context.setVariable("force", event.force)
    }

}