package top.lanscarlos.vulpecula.kether.property.event

import org.bukkit.entity.Entity
import org.bukkit.event.entity.EntityShootBowEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty
import top.lanscarlos.vulpecula.utils.coerceBoolean

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.event
 *
 * @author Lanscarlos
 * @since 2022-11-08 15:12
 */

@VulKetherProperty(
    id = "entity-shoot-event",
    bind = EntityShootBowEvent::class
)
class EntityShootBowEventProperty : VulScriptProperty<EntityShootBowEvent>("entity-shoot-event") {

    override fun readProperty(instance: EntityShootBowEvent, key: String): OpenResult {
        val property: Any? = when (key) {
            "bow", "item" -> instance.bow
            "consumable" -> instance.consumable
            "force" -> instance.force
            "hand" -> instance.hand.name
            "projectile", "arrow" -> instance.projectile
            "should-consume-item", "is-consume-item", "is-consume" -> instance.shouldConsumeItem()
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: EntityShootBowEvent, key: String, value: Any?): OpenResult {
        when (key) {
            "consumable" -> {
                instance.setConsumeItem(value?.coerceBoolean() ?: return OpenResult.successful())
            }
            "projectile" -> {
                instance.projectile = value as? Entity ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}