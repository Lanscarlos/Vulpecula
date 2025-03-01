package top.lanscarlos.vulpecula.common.applicative

import taboolib.common.platform.function.warning
import java.awt.Color

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 14:57
 */
object ColorApplicative : AbstractApplicative<Color>(Color::class.java) {

    val REGEX_HEX = "^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})\$".toRegex()

    val REGEX_RGB = "^\\d+-\\d+-\\d+(-\\d+)?\$".toRegex()

    override fun convert(instance: Any?): Color? {
        return when (instance) {
            is Color -> instance
            is org.bukkit.Color -> Color(instance.red, instance.green, instance.blue)
            is String -> {
                when {
                    instance.startsWith('#') && instance.matches(REGEX_HEX) -> {
                        // hex
                        Color.decode(instance)
                    }
                    instance.matches(REGEX_RGB) -> {
                        val demand = instance.split("-").map { it.toInt().coerceIn(0, 255) }
                        if (demand.size == 4) {
                            // r-g-b-a
                            Color(demand[0], demand[1], demand[2], demand[3])
                        } else {
                            Color(demand[0], demand[1], demand[2])
                        }
                    }
                    else -> {
                        val rgb = instance.toIntOrNull() ?: let {
                            warning("ColorApplicative#apply >> Instance cannot transform to rgb color. $instance")
                            return null
                        }
                        Color(rgb)
                    }
                }
            }
            else -> null
        }
    }

    override fun readProperty(instance: Color, key: String): Any {
        return when (key) {
            "red" -> instance.red
            "green" -> instance.green
            "blue" -> instance.blue
            "alpha" -> instance.alpha
            "rgb" -> instance.rgb
            else -> failedByGetPropertyNotSupported(instance, key)
        }
    }

    override fun writeProperty(instance: Color, key: String, value: Any?) {
        failedBySetPropertyNotSupported(instance, key)
    }
}