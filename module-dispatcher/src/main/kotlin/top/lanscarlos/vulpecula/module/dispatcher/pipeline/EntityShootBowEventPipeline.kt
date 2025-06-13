package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.event.entity.EntityShootBowEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.Context

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * @author Lanscarlos
 * @since 2025/6/12 9:01
 */
@AutoRegistered
class EntityShootBowEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<EntityShootBowEvent>(clazz, config) {

    override fun process(context: Context) {
        TODO("Not yet implemented")
    }

    override fun preprocess(context: Context) {
        val event = getEvent(context)
    }

    override fun postprocess(context: Context) {
        TODO("Not yet implemented")
    }

}