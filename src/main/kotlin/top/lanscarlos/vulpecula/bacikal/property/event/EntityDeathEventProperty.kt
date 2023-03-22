package top.lanscarlos.vulpecula.bacikal.property.event

import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.bacikal.BacikalScriptProperty
import top.lanscarlos.vulpecula.utils.coerceInt

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.event
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:23
 */
@BacikalProperty(
    id = "entity-death-event",
    bind = EntityDeathEvent::class
)
class EntityDeathEventProperty : BacikalScriptProperty<EntityDeathEvent>("entity-death-event") {

    override fun readProperty(instance: EntityDeathEvent, key: String): OpenResult {
        val property: Any? = when (key) {
            "droppedExp", "dropped-exp", "exp" -> instance.droppedExp
            "drops" -> instance.drops
            "cause" -> instance.entity.lastDamageCause?.cause?.name ?: "UNKNOWN"
            "damage", "dmg" -> instance.entity.lastDamageCause?.damage ?: 0.0
            "finalDamage", "final-damage", "final-dmg", "final" -> instance.entity.lastDamageCause?.finalDamage ?: 0.0
            "damager*" -> {
                when (val lastDamageCause = instance.entity.lastDamageCause) {
                    is EntityDamageByBlockEvent -> lastDamageCause.damager
                    is EntityDamageByEntityEvent -> lastDamageCause.damager
                    else -> null
                }
            }
            "damager" -> {
                when (val lastDamageCause = instance.entity.lastDamageCause) {
                    is EntityDamageByBlockEvent -> lastDamageCause.damager
                    is EntityDamageByEntityEvent -> {
                        when (val source = lastDamageCause.damager) {
                            is Projectile -> source.shooter
                            else -> source
                        }
                    }
                    else -> null
                }
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: EntityDeathEvent, key: String, value: Any?): OpenResult {
        when (key) {
            "droppedExp", "dropped-exp", "exp" -> {
                instance.droppedExp = value?.coerceInt() ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}