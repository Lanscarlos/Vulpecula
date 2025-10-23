package top.lanscarlos.vulpecula.module.dispatcher.pipeline.entity

import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.Context
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

    override fun initPrincipal(context: Context) {
        context.setPrincipal(getEvent(context).entity)
    }

    override fun initVariables(context: Context) {
        val event = getEvent(context)
        context.setVariable("entity", event.entity)
    }

}