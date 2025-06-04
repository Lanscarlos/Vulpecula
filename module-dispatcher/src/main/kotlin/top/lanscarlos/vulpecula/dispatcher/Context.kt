package top.lanscarlos.vulpecula.dispatcher

import org.bukkit.entity.Player
import org.bukkit.event.Event

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/4 11:30
 */
data class Context(val event: Event, val player: Player?)
