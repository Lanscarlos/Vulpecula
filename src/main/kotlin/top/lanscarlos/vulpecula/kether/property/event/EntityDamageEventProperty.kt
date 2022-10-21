package top.lanscarlos.vulpecula.kether.property.event

import org.bukkit.entity.Projectile
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty
import top.lanscarlos.vulpecula.utils.toBoolean
import top.lanscarlos.vulpecula.utils.toDouble

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.event
 *
 * @author Lanscarlos
 * @since 2022-10-21 11:33
 */

@VulKetherProperty(
    id = "entity-damage-event",
    bind = EntityDamageEvent::class,
)
class EntityDamageEventProperty : VulScriptProperty<EntityDamageEvent>("entity-damage-event") {

    override fun read(instance: EntityDamageEvent, key: String): OpenResult {
        val property: Any? = when (key) {
            "cause*" -> instance.cause
            "cause" -> instance.cause.name
            "damage" -> instance.damage
            "damager*" -> {
                when (instance) {
                    is EntityDamageByBlockEvent -> instance.damager
                    is EntityDamageByEntityEvent -> instance.damager
                    else -> return OpenResult.failed()
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
                    else -> return OpenResult.failed()
                }
            }
            "finalDamage", "final-damage", "final" -> instance.finalDamage
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun write(instance: EntityDamageEvent, key: String, value: Any?): OpenResult {
        when (key) {
            "damage" -> {
                instance.damage = value?.toDouble() ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}