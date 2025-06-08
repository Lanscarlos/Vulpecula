package top.lanscarlos.vulpecula.module.dispatcher.rule

import org.bukkit.event.entity.EntityShootBowEvent
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ReflexClass

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025/6/6 22:39
 */
class EntityShootBowEventRule(clazz: ReflexClass, config: ConfigurationSection) : EntityEventRule<EntityShootBowEvent>(clazz, config) {

    override fun parseVariables(event: EntityShootBowEvent): Map<String, Any?> {
        return super.parseVariables(event).plus(
            mapOf(
                "projectile" to event.projectile,
                "bow" to event.bow,
                "force" to event.force
            )
        )
    }
}
