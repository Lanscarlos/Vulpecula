package top.lanscarlos.vulpecula.kether.action.target.selector

import org.bukkit.entity.Animals
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.info
import taboolib.library.kether.QuestReader
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.kether.action.target.ActionTarget
import top.lanscarlos.vulpecula.kether.live.BooleanLiveData
import top.lanscarlos.vulpecula.kether.live.DoubleLiveData
import top.lanscarlos.vulpecula.kether.live.LiveData
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
        var includeSelf: LiveData<Boolean> = BooleanLiveData(false)

        while (reader.nextPeek().startsWith('-')) {
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
                "include-self", "self" -> includeSelf = reader.readBoolean()
            }
        }

        return handle { collection ->
            val loc = (center?.getOrNull(this) ?: this.unsafePlayer()?.location)?.toBukkitLocation() ?: error("No loc selected.")

            loc.world?.getNearbyEntities(
                loc,
                radiusX.get(this, 1.0),
                radiusY.get(this, 1.0),
                radiusZ.get(this, 1.0)
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
            if (!includeSelf.get(this, false)) {
                this.unsafePlayer()?.bukkit()?.let {
                    collection.remove(it)
                }
            }

            collection
        }
    }
}