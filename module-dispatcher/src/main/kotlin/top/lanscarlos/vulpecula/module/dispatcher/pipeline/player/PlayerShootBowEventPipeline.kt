package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import org.bukkit.event.entity.EntityShootBowEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.Context
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AutoRegistered

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/6/12 13:21
 */
@AutoRegistered("PlayerShootBowEvent")
class PlayerShootBowEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<EntityShootBowEvent>(clazz, config) {

    override fun filter(context: Context) {
        if (context.player == null) {
            // 玩家必须存在
            context.filter()
        }
    }

}