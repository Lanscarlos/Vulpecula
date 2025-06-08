package top.lanscarlos.vulpecula.module.dispatcher.rule

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerEvent
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ReflexClass

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025/6/4 11:50
 */
abstract class PlayerEventRule<T: PlayerEvent>(clazz: ReflexClass, config: ConfigurationSection) : AbstractRule<T>(clazz, config) {

    override fun parsePlayer(event: T): Player? {
        return event.player
    }

    override fun parseVariables(event: T): Map<String, Any?> {
        return super.parseVariables(event).plus(
            mapOf(
                "player" to event.player
            )
        )
    }

}