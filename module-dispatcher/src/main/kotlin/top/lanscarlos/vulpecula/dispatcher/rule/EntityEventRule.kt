package top.lanscarlos.vulpecula.dispatcher.rule

import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityEvent
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ReflexClass

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025/6/6
 */
abstract class EntityEventRule<T : EntityEvent>(clazz: ReflexClass, config: ConfigurationSection) : AbstractRule<T>(clazz, config) {

    override fun parsePlayer(event: T): Player? {
        return event.entity as? Player
    }

    override fun parseVariables(event: T): Map<String, Any?> {
        return super.parseVariables(event).plus(
            mapOf(
                "entity" to event.entity
            )
        )
    }

}