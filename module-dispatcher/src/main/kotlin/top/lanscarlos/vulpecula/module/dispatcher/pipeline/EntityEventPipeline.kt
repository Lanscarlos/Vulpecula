package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.Context

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * @author Lanscarlos
 * @since 2025/6/14 9:24
 */
@AutoRegistered
class EntityEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<EntityEvent>(clazz, config) {

    override fun initPlayer(context: Context) {
        context.setPlayer(getEvent(context).entity as? Player)
    }

    override fun initVariables(context: Context) {
        val event = getEvent(context)
        context.setVariable("entity", event.entity)
    }

}