package top.lanscarlos.vulpecula.dispatcher.rule

import org.bukkit.event.Event
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.dispatcher.Context

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025-03-12 17:18
 */
class PlayerMoveEventRule(clazz: Class<out Event>, config: ConfigurationSection) : AbstractRule<PlayerMoveEvent>(clazz, config) {
    override fun matches(context: Context): Boolean {
        TODO("Not yet implemented")
    }
}