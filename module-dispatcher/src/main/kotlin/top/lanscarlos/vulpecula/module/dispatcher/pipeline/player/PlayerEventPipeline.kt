package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import org.bukkit.event.player.PlayerEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.PipelineContext
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AutoRegistered

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/6/14 9:51
 */
@AutoRegistered
class PlayerEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<PlayerEvent>(clazz, config) {

    override fun initPrincipal(context: PipelineContext) {
        context.setPrincipal(getEvent(context).player)
    }

}