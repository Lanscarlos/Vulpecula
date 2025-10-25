package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import org.bukkit.event.player.PlayerCommandPreprocessEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/10/25
 */
class PlayerCommandPreprocessEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<PlayerCommandPreprocessEvent>(clazz, config) {
}