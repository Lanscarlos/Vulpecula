package top.lanscarlos.vulpecula.module.property.common

import org.bukkit.Color
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
@Property(bind = Color::class)
object ColorProperty : BacikalProperty<Color> {

    @Suppress("DEPRECATION")
    override fun readProperty(instance: Color, key: String): Any {
        return try {
            when(key) {
                "alpha" -> instance.alpha
                "red" -> instance.red
                "green" -> instance.green
                "blue" -> instance.blue
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    @Suppress("DEPRECATION")
    override fun writeProperty(instance: Color, key: String, value: Any?) {
        try {
            when(key) {
                "alpha" -> instance.alpha = value.let(IntApplicative::convert)
                "red" -> instance.red = value.let(IntApplicative::convert)
                "green" -> instance.green = value.let(IntApplicative::convert)
                "blue" -> instance.blue = value.let(IntApplicative::convert)
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}