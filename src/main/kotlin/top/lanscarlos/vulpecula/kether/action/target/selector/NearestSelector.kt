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
 * @since 2022-11-17 20:23
 */
object NearestSelector : ActionTarget.Reader {

    enum class Type(vararg namespace: String) {
        NearestEntity("nearest-entity", "NearestEntity", "@Nearest"),
        NearestLivingEntity("nearest-living-entity", "NearestLivingEntity"),
        NearestPlayer("nearest-player", "NearestPlayer"),
        NearestAnimal("nearest-animal", "NearestAnimal");

        val namespace = namespace.map { it.lowercase() }
    }

    override val name: Array<String> = Type.values().flatMap { it.namespace.toList() }.toTypedArray()

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionTarget.Handler {
        val type = Type.values().firstOrNull { input in it.namespace }

        var center = reader.tryReadLocation("at")
        var radiusX: LiveData<Double> = DoubleLiveData(1)
        var radiusY: LiveData<Double> = DoubleLiveData(1)
        var radiusZ: LiveData<Double> = DoubleLiveData(1)

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
            }
        }

        return handle { collection ->
            val loc = (center?.getOrNull(this) ?: this.unsafePlayer()?.location)?.toBukkitLocation() ?: error("No loc selected.")

            loc.world?.getNearbyEntities(
                loc,
                radiusX.get(this, 1.0),
                radiusY.get(this, 1.0),
                radiusZ.get(this, 1.0)
            )?.filter { entity ->
                when (type) {
                    Type.NearestEntity -> true
                    Type.NearestLivingEntity -> entity is LivingEntity
                    Type.NearestPlayer -> entity is Player
                    Type.NearestAnimal -> entity is Animals
                    else -> false
                }
            }?.minByOrNull { it.location.distance(loc) }?.let {
                collection += it
            }

            collection
        }
    }
}