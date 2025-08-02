package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.Context
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AutoRegistered

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/8/2
 */
@AutoRegistered("PlayerDamageByPlayerEvent", "PlayerDamageByEntityEvent")
class PlayerDamageByPlayerEventPipeline(clazz: Class<*>, config: ConfigurationSection) : VirtualPlayerEventPipeline<EntityDamageByEntityEvent>(clazz, config) {

    override fun filter(context: Context) {
        val event = getEvent(context)
        if (event.entity !is Player || event.damager !is Player) {
            context.filter()
        }
    }

}