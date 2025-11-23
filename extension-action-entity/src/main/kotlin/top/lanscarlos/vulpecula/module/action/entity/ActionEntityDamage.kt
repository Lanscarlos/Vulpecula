package top.lanscarlos.vulpecula.module.action.entity

import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.annotation.Optional
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.entity
 *
 * @author Lanscarlos
 * @since 2025/7/3
 */
@Parser("entity.damage")
object ActionEntityDamage : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame,
        damage: Double,
        @Optional(["by"]) damager: Entity? = null
    ) {
        val entity = ActionEntity.getContext(frame)
        require(entity is Damageable) {
            asLang("module-action-entity-exception-entity-unsupported-damage", entity.type.name)
        }
        entity.damage(damage, damager)
    }

}
