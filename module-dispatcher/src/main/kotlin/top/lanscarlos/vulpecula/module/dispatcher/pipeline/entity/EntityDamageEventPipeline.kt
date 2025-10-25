package top.lanscarlos.vulpecula.module.dispatcher.pipeline.entity

import org.bukkit.event.entity.EntityDamageEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.applicative.DoubleApplicative
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
class EntityDamageEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<EntityDamageEvent>(clazz, config) {

    override fun initVariables(context: PipelineContext) {
        val event = getEvent(context)
        context.setVariable("event.damage", event.damage)
        context.setVariable("event.cause", event.cause.name)
        context.setVariable("event.finalDamage", event.finalDamage)
    }

    override fun postprocess(context: PipelineContext) {
        val event = getEvent(context)
        event.damage = context.getVariable("damage", DoubleApplicative)
    }

}