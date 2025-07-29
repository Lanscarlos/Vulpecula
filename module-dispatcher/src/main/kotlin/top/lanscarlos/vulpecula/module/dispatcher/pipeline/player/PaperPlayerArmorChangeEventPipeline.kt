package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.Context
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

    override fun initVariables(context: Context) {
        val event = getEvent(context)
        context.setVariable("slot", event.slotType.name)
        context.setVariable("oldItem", event.oldItem)
        context.setVariable("newItem", event.newItem)
    }

}