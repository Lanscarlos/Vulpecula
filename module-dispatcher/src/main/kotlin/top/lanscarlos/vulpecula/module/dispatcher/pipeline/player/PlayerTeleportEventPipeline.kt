package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AutoRegistered
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.PipelineContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/10/25
 */
@AutoRegistered
class PlayerTeleportEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<PlayerTeleportEvent>(clazz, config) {

    override fun initVariables(context: PipelineContext) {
        val event = getEvent(context)
        context.setVariable("event.cause", event.cause.name)
    }

}