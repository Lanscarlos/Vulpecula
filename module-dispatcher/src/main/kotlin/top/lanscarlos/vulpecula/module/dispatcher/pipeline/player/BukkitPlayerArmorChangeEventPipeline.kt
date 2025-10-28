package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.applicative.ItemStackApplicative
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.PipelineContext
import top.lanscarlos.vulpecula.module.dispatcher.event.PlayerArmorChangeEvent
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AutoRegistered

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/7/10 11:46
 */
@AutoRegistered
class BukkitPlayerArmorChangeEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<PlayerArmorChangeEvent>(clazz, config) {

    override fun initVariables(context: PipelineContext) {
        val event = getEvent(context)
        context.setVariable("event.slot", event.slot.name)
        context.setVariable("event.oldItem", event.oldItem)
        context.setVariable("event.newItem", event.newItem)
    }

    override fun postprocess(context: PipelineContext) {
        val event = getEvent(context)
        event.newItem = context.getVariable("event.newItem", ItemStackApplicative)
    }

}