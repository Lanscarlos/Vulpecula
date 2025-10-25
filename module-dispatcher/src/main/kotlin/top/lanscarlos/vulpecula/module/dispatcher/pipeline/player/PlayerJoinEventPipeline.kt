package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import org.bukkit.event.player.PlayerJoinEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.PipelineContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/10/25
 */
class PlayerJoinEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<PlayerJoinEvent>(clazz, config) {

    override fun initVariables(context: PipelineContext) {
        val event = getEvent(context)
        context.setVariable("event.joinMessage", event.joinMessage)
    }

}