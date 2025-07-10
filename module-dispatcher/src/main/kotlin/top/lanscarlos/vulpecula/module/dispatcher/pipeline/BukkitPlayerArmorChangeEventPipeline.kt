package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.Context
import top.lanscarlos.vulpecula.module.dispatcher.event.PlayerArmorChangeEvent

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * @author Lanscarlos
 * @since 2025/7/10 11:46
 */
@AutoRegistered
class BukkitPlayerArmorChangeEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<PlayerArmorChangeEvent>(clazz, config) {

    override fun initVariables(context: Context) {
        val event = getEvent(context)
        context.setVariable("slot", event.slot.name)
        context.setVariable("oldItem", event.oldItem)
        context.setVariable("newItem", event.newItem)
    }

}