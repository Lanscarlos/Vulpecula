package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.event.player.PlayerEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.Context

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * @author Lanscarlos
 * @since 2025/6/14 9:51
 */
@AutoRegistered
class PlayerEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<PlayerEvent>(clazz, config) {

    override fun initPlayer(context: Context) {
        context.setPlayer(getEvent(context).player)
    }

    override fun initVariables(context: Context) {
        val event = getEvent(context)
        context.setVariable("player", event.player)
    }

}