package top.lanscarlos.vulpecula.module.action.entity

import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import taboolib.common.platform.function.console
import top.lanscarlos.vulpecula.common.lang.Lang
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
            Lang.ACTION_ENTITY_EXCEPTION_ENTITY_UNSUPPORTED_DAMAGE.asText(console(), entity.type.name)
        }
        entity.damage(damage, damager)
    }

}
