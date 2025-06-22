package top.lanscarlos.vulpecula.legacy.bacikal.property

import taboolib.common.OpenResult
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalGenericProperty
import top.lanscarlos.vulpecula.legacy.utils.coerceDouble
import top.lanscarlos.vulpecula.legacy.utils.coerceFloat

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property
 *
 * @author Lanscarlos
 * @since 2023-03-22 13:56
 */
@BacikalProperty(
    id = "block",
    bind = Location::class
)
class LocationTabooProperty : BacikalGenericProperty<Location>("location-taboo") {

    override fun readProperty(instance: Location, key: String): OpenResult {
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
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: Location, key: String, value: Any?): OpenResult {
        when (key) {
            "direction" -> {
                instance.direction = value as? Vector ?: return OpenResult.successful()
            }
            "pitch" -> {
                instance.pitch = value?.coerceFloat(0f) ?: return OpenResult.successful()
            }
            "x" -> {
                instance.x = value?.coerceDouble() ?: return OpenResult.successful()
            }
            "y" -> {
                instance.y = value?.coerceDouble() ?: return OpenResult.successful()
            }
            "yaw" -> {
                instance.yaw = value?.coerceFloat() ?: return OpenResult.successful()
            }
            "z" -> {
                instance.z = value?.coerceDouble() ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }

}