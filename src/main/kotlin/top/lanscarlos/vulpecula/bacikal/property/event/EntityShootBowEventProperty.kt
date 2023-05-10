package top.lanscarlos.vulpecula.bacikal.property.event

import org.bukkit.entity.Entity
import org.bukkit.event.entity.EntityShootBowEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.bacikal.BacikalGenericProperty
import top.lanscarlos.vulpecula.utils.coerceBoolean

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.event
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:24
 */
@BacikalProperty(
    id = "entity-shoot-event",
    bind = EntityShootBowEvent::class
)
class EntityShootBowEventProperty : BacikalGenericProperty<EntityShootBowEvent>("entity-shoot-event") {

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