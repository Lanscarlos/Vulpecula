package top.lanscarlos.vulpecula.bacikal.action.target.selector

import org.bukkit.Location
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
 * @since 2023-03-23 09:27
 */
object ActionTargetSelectInRing : ActionTarget.Resolver {

    private enum class Type(vararg namespace: String) {
        EntitiesInRing("EntitiesInRing", "EntityInRing"),
        LivingEntitiesInRing("LivingEntitiesInRing", "LivingEntityInRing"),
        PlayersInRing("PlayersInRing", "PlayerInRing"),
        AnimalsInRing("AnimalsInRing", "AnimalInRing");

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
                argument("min", then = double(), def = 1.0),
                argument("max", then = double(), def = 1.0),
                argument("min-x", then = double()),
                argument("min-y", then = double()),
                argument("min-z", then = double()),
                argument("max-x", then = double()),
                argument("max-y", then = double()),
                argument("max-z", then = double())
            ) { target, center, self, min, max, minX, minY, minZ, maxX, maxY, maxZ ->

                val loc = (center ?: this.playerOrNull()?.location)?.toBukkitLocation()
                    ?: error("No center location selected.")

                // 创建排除区域
                val area =
                    Location(
                        loc.world,
                        loc.x - (minX ?: min),
                        loc.y - (minY ?: min),
                        loc.z - (minZ ?: min)
                    ) to Location(
                        loc.world,
                        loc.x + (minX ?: min),
                        loc.y + (minY ?: min),
                        loc.z + (minZ ?: min)
                    )

                // 获取实体集合
                val entities = loc.world?.getNearbyEntities(
                    loc,
                    maxX ?: max,
                    maxY ?: max,
                    maxZ ?: max
                ) ?: return@combine target

                for (entity in entities) {

                    val filtered = when (type) {
                        Type.EntitiesInRing -> true
                        Type.LivingEntitiesInRing -> entity is LivingEntity
                        Type.PlayersInRing -> entity is Player
                        Type.AnimalsInRing -> entity is Animals
                    }

                    // 排除类型 以及 区域外的实体
                    if (!filtered || entity.location !in area) continue
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

    /**
     * 给定两点形成的区域，判断指定位置是否在该区域内
     * @param this first 需为最小值点 second 需为最大值点
     * @param element 给定点
     * @return 处于区域内返回 true 反之 false
     * */
    private operator fun Pair<Location, Location>.contains(element: Location): Boolean {
        // 所在世界不一致
        if (first.world != second.world || element.world != first.world) return false
        // 给定点在最小值点外
        if (element.x < first.x || element.y < first.y || element.z < first.z) return false
        // 给定点在最大值点外
        if (element.x > second.x || element.y > second.y || element.z > second.z) return false
        return true
    }
}