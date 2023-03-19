package top.lanscarlos.vulpecula.bacikal.action.entity

import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.warning

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.entity
 *
 * @author Lanscarlos
 * @since 2022-11-17 23:09
 */
object EntityDamageHandler : ActionEntity.Resolver {

    override val name: Array<String> = arrayOf("damage", "dmg")

    /*
    * entity damage &entity &damage
    * entity damage &entity &damage by &damager
    * */
    override fun resolve(reader: ActionEntity.Reader): ActionEntity.Handler<out Any?> {
        return reader.transfer {
            combine(
                source(),
                double(0.0),
                optional("by", then = entity(display = "damager"))
            ) { entity, damage, damager ->
                if (entity !is LivingEntity) {
                    warning("Cannot damage this type of entity: ${entity.type.name} [ERROR: entity@${reader.token}]")
                    return@combine entity
                }
                entity.also { it.damage(damage, damager) }
            }
        }
    }
}