package top.lanscarlos.vulpecula.dispatcher.throttle

import org.bukkit.event.player.PlayerMoveEvent
import top.lanscarlos.vulpecula.common.config.ConfigSource

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.throttle
 *
 * @author Lanscarlos
 * @since 2025-03-09 19:36
 */
class PlayerMoveEventResolver(config: ConfigSource) : ThrottleResolver<PlayerMoveEvent> {

    /**
     * 是否监听视角变化
     * */
    val listenViewChange by config.readBoolean("rule.listen-view-change", false)

    /**
     * 最小监听距离
     * */
    val listenMinDistance by config.readDouble("rule.listen-min-distance", 1e-3)

    override fun resolve(event: PlayerMoveEvent): Boolean {
        TODO("Not yet implemented")
    }

    override fun filter(event: PlayerMoveEvent): Boolean {
        TODO("Not yet implemented")
    }

    override fun baffle(event: PlayerMoveEvent): Boolean {
        TODO("Not yet implemented")
    }

}