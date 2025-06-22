package top.lanscarlos.vulpecula.core.modularity.pipeline

import org.bukkit.event.player.PlayerMoveEvent
import top.lanscarlos.vulpecula.config.DynamicConfig

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.modularity.pipeline
 *
 * @author Lanscarlos
 * @since 2024-01-03 00:23
 */
class MovePipeline(config: DynamicConfig) : AbstractPipeline<PlayerMoveEvent>(config) {

    /**
     * 是否忽略视角变动
     * */
    val ignoreView by config.readBoolean("ignore-view", true)

    override fun filter(event: PlayerMoveEvent): Boolean {
        if (ignoreView) {
            if (event.from.world != event.to?.world) {
                // 忽略跨世界移动
                return true
            }
            val to = event.to ?: return true
            return event.from.distanceSquared(to) > 1e-3
        }
        return true
    }

    override fun preprocess(event: PlayerMoveEvent) {
    }

    override fun variables(event: PlayerMoveEvent): Map<String, Any?> {
        return mapOf(
            "from" to event.from,
            "to" to event.to
        )
    }
}