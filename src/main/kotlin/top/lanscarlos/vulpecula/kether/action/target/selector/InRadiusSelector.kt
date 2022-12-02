package top.lanscarlos.vulpecula.kether.action.target.selector

import org.bukkit.entity.Animals
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.util.Location
import taboolib.library.kether.QuestReader
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.kether.action.target.ActionTarget
import top.lanscarlos.vulpecula.kether.live.*
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.target.selector
 *
 * @author Lanscarlos
 * @since 2022-11-17 20:19
 */
object InRadiusSelector : ActionTarget.Reader {

    enum class Type(vararg namespace: String) {
        EntitiesInRadius("entities-in-radius", "entity-in-radius", "EntitiesInRadius", "EntityInRadius", "EIR"),
        LivingEntitiesInRadius("living-entities-in-radius", "living-entity-in-radius", "LivingEntitiesInRadius", "LivingEntityInRadius", "LEIR"),
        PlayersInRadius("players-in-radius", "player-in-radius", "PlayersInRadius", "PlayerInRadius", "PIR"),
        AnimalsInRadius("animals-in-radius", "animal-in-radius", "AnimalsInRadius", "AnimalInRadius");

        val namespace = namespace.map { it.lowercase() }
    }

    override val name: Array<String> = Type.values().flatMap { it.namespace.toList() }.toTypedArray()

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionTarget.Handler {
        val type = Type.values().firstOrNull { input.lowercase() in it.namespace }

        var center = reader.tryReadLocation("at")
        var radiusX: LiveData<Double> = DoubleLiveData(1)
        var radiusY: LiveData<Double> = DoubleLiveData(1)
        var radiusZ: LiveData<Double> = DoubleLiveData(1)
        var includeSelf = false

        while (reader.nextPeek().startsWith('-')) {

            if (reader.nextPeek() == "-self") {
                includeSelf = true
                continue
            }

            when (reader.nextToken().substring(1)) {
                "location", "loc" -> center = reader.readLocation()
                "radius", "r" -> {
                    val radius = reader.readDouble()
                    radiusX = radius
                    radiusY = radius
                    radiusZ = radius
                }
                "radius-x", "r-x", "x" -> radiusX = reader.readDouble()
                "radius-y", "r-y", "y" -> radiusY = reader.readDouble()
                "radius-z", "r-z", "z" -> radiusZ = reader.readDouble()
            }
        }

        return handleFuture { collection ->
            listOf(
                center?.getOrNull(this),
                radiusX.getOrNull(this),
                radiusY.getOrNull(this),
                radiusZ.getOrNull(this)
            ).thenTake().thenApply { args ->
                val loc = (args[0] as? Location ?: this.playerOrNull()?.location)?.toBukkitLocation() ?: error("No location selected.")

                loc.world?.getNearbyEntities(
                    loc,
                    args[1].coerceDouble(1.0),
                    args[2].coerceDouble(1.0),
                    args[3].coerceDouble(1.0)
                )?.forEach { entity ->
                    val filtered = when (type) {
                        Type.EntitiesInRadius -> true
                        Type.LivingEntitiesInRadius -> entity is LivingEntity
                        Type.PlayersInRadius -> entity is Player
                        Type.AnimalsInRadius -> entity is Animals
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