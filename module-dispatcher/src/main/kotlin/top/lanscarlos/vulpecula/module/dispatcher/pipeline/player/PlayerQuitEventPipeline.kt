package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import org.bukkit.event.player.PlayerQuitEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/10/25
 */
class PlayerQuitEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<PlayerQuitEvent>(clazz, config) {
}