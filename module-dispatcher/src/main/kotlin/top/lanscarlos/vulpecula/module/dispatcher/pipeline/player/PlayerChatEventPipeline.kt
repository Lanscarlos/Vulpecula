package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.applicative.StringApplicative
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.PipelineContext

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/10/25
 */
class PlayerChatEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<AsyncPlayerChatEvent>(clazz, config) {

    override fun initVariables(context: PipelineContext) {
        val event = getEvent(context)
        context.setVariable("event.format", event.format)
        context.setVariable("event.message", event.message)
        context.setVariable("event.recipients", event.recipients)
    }

    override fun postprocess(context: PipelineContext) {
        val event = getEvent(context)
        event.format = context.getVariable("event.format", StringApplicative)
        event.message = context.getVariable("event.message", StringApplicative)
    }

}