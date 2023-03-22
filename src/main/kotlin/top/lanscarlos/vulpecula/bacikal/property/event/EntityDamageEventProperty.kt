package top.lanscarlos.vulpecula.bacikal.property.event

import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.bacikal.BacikalScriptProperty
import top.lanscarlos.vulpecula.utils.coerceDouble

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.event
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:22
 */
@BacikalProperty(
    id = "entity-damage-event",
    bind = EntityDamageEvent::class
)
class EntityDamageEventProperty : BacikalScriptProperty<EntityDamageEvent>("entity-damage-event") {

    override fun readProperty(instance: EntityDamageEvent, key: String): OpenResult {
        val property: Any? = when (key) {
            "cause*" -> instance.cause
            "cause" -> instance.cause.name
            "damage", "dmg" -> instance.damage
            "damager*" -> {
                when (instance) {
                    is EntityDamageByBlockEvent -> instance.damager
                    is EntityDamageByEntityEvent -> instance.damager
                    else -> null
                }
            }
            "damager" -> {
                when (instance) {
                    is EntityDamageByBlockEvent -> instance.damager
                    is EntityDamageByEntityEvent -> {
                        when (val source = instance.damager) {
                            is Projectile -> source.shooter
                            else -> source
                        }
                    }
                    else -> null
                }
            }
            "finalDamage", "final-damage", "final-dmg", "final" -> instance.finalDamage
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: EntityDamageEvent, key: String, value: Any?): OpenResult {
        when (key) {
            "damage" -> {
                instance.damage = value?.coerceDouble() ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}