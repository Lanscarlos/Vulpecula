package top.lanscarlos.vulpecula.module.property.common

import org.bukkit.Location
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalProperty
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.module.bacikal.annotation.Property

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property.common
 *
 * @author Lanscarlos
 * @since 2025/10/11
 */
@Property(bind = Location::class)
object LocationProperty : BacikalProperty<Location> {

    @Suppress("DEPRECATION")
    override fun readProperty(instance: Location, key: String): Any? {
        return try {
            when(key) {
                "world" -> instance.world
                "chunk" -> instance.chunk
                "block" -> instance.block
                "x" -> instance.x
                "blockX" -> instance.blockX
                "y" -> instance.y
                "blockY" -> instance.blockY
                "z" -> instance.z
                "blockZ" -> instance.blockZ
                "yaw" -> instance.yaw
                "pitch" -> instance.pitch
                "direction" -> instance.direction
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    @Suppress("DEPRECATION")
    override fun writeProperty(instance: Location, key: String, value: Any?) {
        try {
            when(key) {
                "x" -> instance.x = value.let(DoubleApplicative::convert)
                "y" -> instance.y = value.let(DoubleApplicative::convert)
                "z" -> instance.z = value.let(DoubleApplicative::convert)
                "yaw" -> instance.yaw = value.let(FloatApplicative::convert)
                "pitch" -> instance.pitch = value.let(FloatApplicative::convert)
                "direction" -> instance.direction = value.let(BukkitVectorApplicative::convert)

                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}