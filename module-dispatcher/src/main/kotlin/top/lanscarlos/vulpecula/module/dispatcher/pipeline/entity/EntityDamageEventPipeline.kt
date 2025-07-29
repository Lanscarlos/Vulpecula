package top.lanscarlos.vulpecula.module.dispatcher.pipeline.entity

import org.bukkit.event.entity.EntityDamageEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.applicative.DoubleApplicative
import top.lanscarlos.vulpecula.module.dispatcher.Context
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

    override fun initVariables(context: Context) {
        val event = getEvent(context)
        context.setVariable("damage", event.damage)
        context.setVariable("cause", event.cause.name)
        context.setVariable("finalDamage", event.finalDamage)
    }

    override fun postprocess(context: Context) {
        val event = getEvent(context)
        event.damage = context.getVariable("damage", DoubleApplicative)
    }

}