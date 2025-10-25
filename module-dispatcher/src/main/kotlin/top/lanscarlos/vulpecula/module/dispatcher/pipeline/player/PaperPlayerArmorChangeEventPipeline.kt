package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.PipelineContext
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AutoRegistered

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
@AutoRegistered
class PaperPlayerArmorChangeEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<PlayerArmorChangeEvent>(clazz, config) {

    override fun initVariables(context: PipelineContext) {
        val event = getEvent(context)
        context.setVariable("event.slot", event.slotType.name)
        context.setVariable("event.oldItem", event.oldItem)
        context.setVariable("event.newItem", event.newItem)
    }

}