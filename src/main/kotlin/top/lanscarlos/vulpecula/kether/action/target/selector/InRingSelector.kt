package top.lanscarlos.vulpecula.kether.action.target.selector

import org.bukkit.entity.Animals
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.library.kether.QuestReader
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.kether.action.target.ActionTarget
import top.lanscarlos.vulpecula.kether.live.DoubleLiveData
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.target.selector
 *
 * @author Lanscarlos
 * @since 2022-11-17 20:58
 */
object InRingSelector : ActionTarget.Reader {

    enum class Type(vararg namespace: String) {
        EntitiesInRing("entities-in-ring", "entity-in-ring", "EntitiesInRing", "EntityInRing"),
        LivingEntitiesInRing("living-entities-in-ring", "living-entity-in-ring", "LivingEntitiesInRing", "LivingEntityInRing"),
        PlayersInRing("players-in-ring", "player-in-ring", "PlayersInRing", "PlayerInRing"),
        AnimalsInRing("animals-in-ring", "animal-in-ring", "AnimalsInRing", "AnimalInRing");

        val namespace = namespace.map { it.lowercase() }
    }

    override val name: Array<String> = Type.values().flatMap { it.namespace.toList() }.toTypedArray()

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionTarget.Handler {
        val type = Type.values().firstOrNull { input in it.namespace }

        var center = reader.tryReadLocation("at")
        var minX: LiveData<Double> = DoubleLiveData(1)
        var minY: LiveData<Double> = DoubleLiveData(1)
        var minZ: LiveData<Double> = DoubleLiveData(1)

        var maxX: LiveData<Double> = DoubleLiveData(1)
        var maxY: LiveData<Double> = DoubleLiveData(1)
        var maxZ: LiveData<Double> = DoubleLiveData(1)

        while (reader.nextPeek().startsWith('-')) {
            when (reader.nextToken().substring(1)) {
                "location", "loc" -> center = reader.readLocation()
                "min" -> {
                    val radius = reader.readDouble()
                    minX = radius
                    minY = radius
                    minZ = radius
                }
                "max" -> {
                    val radius = reader.readDouble()
                    maxX = radius
                    maxY = radius
                    maxZ = radius
                }
                "min-x" -> minX = reader.readDouble()
                "min-y" -> minY = reader.readDouble()
                "min-z" -> minZ = reader.readDouble()
                "max-x" -> maxX = reader.readDouble()
                "max-y" -> maxY = reader.readDouble()
                "max-z" -> maxZ = reader.readDouble()
            }
        }

        return handle { collection ->
            val loc = (center?.getOrNull(this) ?: this.unsafePlayer()?.location)?.toBukkitLocation() ?: error("No loc selected.")

            // 获取最小圈内实体（排除）
            val exclude = loc.world?.getNearbyEntities(
                loc,
                minX.get(this, 1.0),
                minY.get(this, 1.0),
                minZ.get(this, 1.0)
            ) ?: setOf()

            // 获取最大圈内所有实体
            loc.world?.getNearbyEntities(
                loc,
                maxX.get(this, 1.0),
                maxY.get(this, 1.0),
                maxZ.get(this, 1.0)
            )?.forEach { entity ->
                if (entity in exclude) return@forEach
                val filtered = when (type) {
                    Type.EntitiesInRing -> true
                    Type.LivingEntitiesInRing -> entity is LivingEntity
                    Type.PlayersInRing -> entity is Player
                    Type.AnimalsInRing -> entity is Animals
                    else -> false
                }
                if (filtered) collection += entity
            }

            collection
        }
    }
}