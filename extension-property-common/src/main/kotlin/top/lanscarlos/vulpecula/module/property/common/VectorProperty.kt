package top.lanscarlos.vulpecula.module.property.common

import org.bukkit.util.Vector
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
@Property(bind = Vector::class)
object VectorProperty : BacikalProperty<Vector> {

    @Suppress("DEPRECATION")
    override fun readProperty(instance: Vector, key: String): Any {
        return try {
            when(key) {
                "x" -> instance.getX()
                "blockX" -> instance.blockX
                "y" -> instance.getY()
                "blockY" -> instance.blockY
                "z" -> instance.getZ()
                "blockZ" -> instance.blockZ
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    @Suppress("DEPRECATION")
    override fun writeProperty(instance: Vector, key: String, value: Any?) {
        try {
            when(key) {
                "x" -> instance.setX(value.let(DoubleApplicative::convert))
                "y" -> instance.setY(value.let(DoubleApplicative::convert))
                "z" -> instance.setZ(value.let(DoubleApplicative::convert))
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}