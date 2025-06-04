package top.lanscarlos.vulpecula.dispatcher.rule

import org.bukkit.event.player.PlayerMoveEvent
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ReflexClass

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025/6/4 11:50
 */
class PlayerMoveEventRule(clazz: ReflexClass, config: ConfigurationSection) : PlayerEventRule<PlayerMoveEvent>(clazz, config) {
}