package top.lanscarlos.vulpecula.dispatcher.throttle

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import top.lanscarlos.vulpecula.common.config.ConfigSource

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.throttle
 *
 * @author Lanscarlos
 * @since 2025-03-10 13:24
 */
class DefaultThrottler<T: Event>(config: ConfigSource) : Throttler<T> {

    /**
     * 是否监听已取消事件
     * */
    val listenCancelled by config.readBoolean("rule.listen-cancelled", false)

    /**
     * 监听冷却
     * */
    val listenCooldown by config.readInt("rule.listen-cooldown", -1)

    val resolver: ThrottleResolver<T> = getResolver()

    val lastTime: Long = -1

    override fun throttle(event: T): Boolean {
        if (event is Cancellable) {
            if (event.isCancelled && !listenCancelled) {
                return false
            }
        }

        val result = resolver.resolve(event)
        if (result) {
        }
        return result
    }

    companion object {

        fun <T: Event> getResolver(): ThrottleResolver<T> {
            return resolver
        }

    }

}