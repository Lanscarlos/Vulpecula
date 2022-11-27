package top.lanscarlos.vulpecula.kether.action.target.selector

import org.bukkit.entity.Animals
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.util.Location
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
        val type = Type.values().firstOrNull { input.lowercase() in it.namespace }

        var center = reader.tryReadLocation("at")
        var minX: LiveData<Double> = DoubleLiveData(1)
        var minY: LiveData<Double> = DoubleLiveData(1)
        var minZ: LiveData<Double> = DoubleLiveData(1)

        var maxX: LiveData<Double> = DoubleLiveData(1)
        var maxY: LiveData<Double> = DoubleLiveData(1)
        var maxZ: LiveData<Double> = DoubleLiveData(1)

        var includeSelf = false

        while (reader.nextPeek().startsWith('-')) {

            if (reader.nextPeek() == "-self") {
                includeSelf = true
                continue
            }

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

        return handleFuture { collection ->
            listOf(
                center?.getOrNull(this),

                minX.getOrNull(this),
                minY.getOrNull(this),
                minZ.getOrNull(this),

                maxX.getOrNull(this),
                maxY.getOrNull(this),
                maxZ.getOrNull(this)
            ).thenTake().thenApply { args ->
                val loc = (args[0] as? Location ?: this.playerOrNull()?.location)?.toBukkitLocation() ?: error("No location selected.")

                val exclude = loc.world?.getNearbyEntities(
                    loc,
                    args[1].coerceDouble(1.0),
                    args[2].coerceDouble(1.0),
                    args[3].coerceDouble(1.0)
                ) ?: setOf()

                loc.world?.getNearbyEntities(
                    loc,
                    args[4].coerceDouble(1.0),
                    args[5].coerceDouble(1.0),
                    args[6].coerceDouble(1.0)
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

                // 排除自己
                if (!includeSelf) {
                    this.playerOrNull()?.toBukkit()?.let {
                        collection.remove(it)
                    }
                }

                return@thenApply collection
            }
        }
    }
}