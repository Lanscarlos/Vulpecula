package top.lanscarlos.vulpecula.core.modularity.pipeline

import org.bukkit.event.player.PlayerEvent
import top.lanscarlos.vulpecula.config.DynamicConfig

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.modularity.pipeline
 *
 * @author Lanscarlos
 * @since 2024-01-03 00:33
 */
class PlayerPipeline(config: DynamicConfig) : AbstractPipeline<PlayerEvent>(config) {
    override fun filter(event: PlayerEvent): Boolean {
        return true
    }

    override fun preprocess(event: PlayerEvent) {
    }

    override fun variables(event: PlayerEvent): Map<String, Any?> {
        return mapOf(VARIABLE_PLAYER to event.player)
    }
}