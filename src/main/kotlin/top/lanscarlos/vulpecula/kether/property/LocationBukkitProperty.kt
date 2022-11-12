package top.lanscarlos.vulpecula.kether.property

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import taboolib.common.OpenResult
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty
import top.lanscarlos.vulpecula.utils.toDouble
import top.lanscarlos.vulpecula.utils.toFloat

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property
 *
 * @author Lanscarlos
 * @since 2022-10-20 11:44
 */

@VulKetherProperty(
    id = "location-bukkit",
    bind = Location::class
)
class LocationBukkitProperty : VulScriptProperty<Location>("location-bukkit") {

    override fun readProperty(instance: Location, key: String): OpenResult {
        val property: Any? = when (key) {
            "clone" -> instance.clone()
            "taboo" -> instance.toProxyLocation()
            "block" -> instance.block
            "blockX", "block-x" -> instance.blockX
            "blockY", "block-y" -> instance.blockY
            "blockZ", "block-z" -> instance.blockZ
            "chunk" -> instance.chunk
            "direction" -> instance.direction
            "pitch" -> instance.pitch
            "world" -> instance.world
            "x" -> instance.x
            "y" -> instance.y
            "yaw" -> instance.yaw
            "z" -> instance.z
            "isWorldLoaded", "world-loaded", "loaded" -> instance.isWorldLoaded
            "length" -> instance.length()
            "lengthSquared", "length-squared", "squared", "sq" -> instance.lengthSquared()
            "serialize" -> instance.serialize()
            "toVector", "vector" -> instance.toVector()
            "toString", "string" -> instance.toString()
            "zero" -> instance.zero()
            else -> OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: Location, key: String, value: Any?): OpenResult {
        when (key) {
            "direction" -> {
                instance.direction = value as? Vector ?: return OpenResult.successful()
            }
            "pitch" -> {
                instance.pitch = value?.toFloat(0f) ?: return OpenResult.successful()
            }
            "world" -> {
                val world = when (value) {
                    is World -> value
                    is String -> Bukkit.getWorld(value)
                    is Location -> value.world
                    else -> null
                }
                instance.world = world ?: return OpenResult.successful()
            }
            "x" -> {
                instance.x = value?.toDouble() ?: return OpenResult.successful()
            }
            "y" -> {
                instance.y = value?.toDouble() ?: return OpenResult.successful()
            }
            "yaw" -> {
                instance.yaw = value?.toFloat() ?: return OpenResult.successful()
            }
            "z" -> {
                instance.z = value?.toDouble() ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }

}