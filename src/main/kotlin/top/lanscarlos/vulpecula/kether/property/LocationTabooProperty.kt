package top.lanscarlos.vulpecula.kether.property

import taboolib.common.OpenResult
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty
import top.lanscarlos.vulpecula.utils.toDouble
import top.lanscarlos.vulpecula.utils.toFloat

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property
 *
 * @author Lanscarlos
 * @since 2022-11-11 19:58
 */

/**
 * Location 泛型属性
 * */
@VulKetherProperty(
    id = "location-taboo-generic",
    bind = Location::class,
    generic = true
)
class LocationTabooGenericProperty : GenericProperty<Location>("location-taboo-generic")

@VulKetherProperty(
    id = "location-taboo",
    bind = Location::class
)
class LocationTabooProperty : VulScriptProperty<Location>("location-taboo") {

    override fun read(instance: Location, key: String): OpenResult {
        val property: Any? = when (key) {
            "clone" -> instance.clone()
            "bukkit" -> instance.toBukkitLocation()
            "blockX", "block-x" -> instance.blockX
            "blockY", "block-y" -> instance.blockY
            "blockZ", "block-z" -> instance.blockZ
            "direction" -> instance.direction
            "pitch" -> instance.pitch
            "world" -> instance.world
            "x" -> instance.x
            "y" -> instance.y
            "yaw" -> instance.yaw
            "z" -> instance.z
            "length" -> instance.length()
            "lengthSquared", "length-squared", "length-sq", "squared", "sq" -> instance.lengthSquared()
            "toVector", "vector" -> instance.toVector()
            "toString", "string" -> instance.toString()
            "zero" -> instance.zero()
            else -> OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun write(instance: Location, key: String, value: Any?): OpenResult {
        when (key) {
            "direction" -> {
                instance.direction = value as? Vector ?: return OpenResult.successful()
            }
            "pitch" -> {
                instance.pitch = value?.toFloat(0f) ?: return OpenResult.successful()
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