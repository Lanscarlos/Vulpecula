package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import org.bukkit.inventory.EquipmentSlot
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.Context

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
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