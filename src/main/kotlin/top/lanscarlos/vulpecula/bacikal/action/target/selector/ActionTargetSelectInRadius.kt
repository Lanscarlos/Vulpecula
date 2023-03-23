package top.lanscarlos.vulpecula.bacikal.action.target.selector

import org.bukkit.entity.Animals
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.bacikal.action.target.ActionTarget
import top.lanscarlos.vulpecula.utils.playerOrNull
import top.lanscarlos.vulpecula.utils.toBukkit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.target.selector
 *
 * @author Lanscarlos
 * @since 2023-03-22 23:59
 */
object ActionTargetSelectInRadius : ActionTarget.Resolver {

    private enum class Type(vararg namespace: String) {
        EntitiesInRadius("EntitiesInRadius", "EntityInRadius", "EIR"),
        LivingEntitiesInRadius("LivingEntitiesInRadius", "LivingEntityInRadius", "LEIR"),
        PlayersInRadius("PlayersInRadius", "PlayerInRadius", "PIR"),
        AnimalsInRadius("AnimalsInRadius", "AnimalInRadius", "AIR");

        val namespace = namespace.map { it.lowercase() }
    }

    override val name: Array<String> = Type.values().flatMap { it.namespace }.toTypedArray()

    override fun resolve(reader: ActionTarget.Reader): ActionTarget.Handler<out Any?> {
        val type = Type.values().first { reader.token in it.namespace }

        return reader.transfer {
            combine(
                source(),
                optional("at", then = location()), // center
                argument("self", then = LiveData.point(true), def = false),
                argument("radius", "r", then = double(), def = 1.0),
                argument("radius-x", "r-x", then = double()),
                argument("radius-y", "r-y", then = double()),
                argument("radius-z", "r-z", then = double())
            ) { target, center, self, radius, radiusX, radiusY, radiusZ ->

                val loc = (center ?: this.playerOrNull()?.location)?.toBukkitLocation()
                    ?: error("No center location selected.")

                // 获取实体集合
                val entities = loc.world?.getNearbyEntities(
                    loc,
                    radiusX ?: radius,
                    radiusY ?: radius,
                    radiusZ ?: radius
                ) ?: return@combine target

                // 遍历实体集合
                for (entity in entities) {
                    val filtered = when (type) {
                        Type.EntitiesInRadius -> true
                        Type.LivingEntitiesInRadius -> entity is LivingEntity
                        Type.PlayersInRadius -> entity is Player
                        Type.AnimalsInRadius -> entity is Animals
                    }

                    // 排除类型
                    if (filtered) continue
                    target += entity
                }

                // 排除自己
                if (!self) {
                    this.playerOrNull()?.toBukkit()?.let {
                        target -= it
                    }
                }

                target
            }
        }
    }
}