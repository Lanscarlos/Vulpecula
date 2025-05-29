package top.lanscarlos.vulpecula.common.applicative

import java.awt.Color

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 14:57
 */
object ColorApplicative : AbstractApplicative<Color>(Color::class.java) {

    private val REGEX_HEX = "^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})\$".toRegex()

    private val REGEX_RGB = "^\\d+-\\d+-\\d+(-\\d+)?\$".toRegex()

    override fun convert(instance: Any): Color {
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
                        val rgb = instance.toIntOrNull() ?: throw InvalidValueException(instance, Color::class.java)
                        Color(rgb)
                    }
                }
            }
            else -> throw UnsupportedTypeException(instance::class.java, Color::class.java)
        }
    }

    override fun readProperty(instance: Color, key: String): Any {
        return when (key) {
            "red" -> instance.red
            "green" -> instance.green
            "blue" -> instance.blue
            "alpha" -> instance.alpha
            "rgb" -> instance.rgb
            else -> errorGetPropertyNotSupported(instance, key)
        }
    }

    override fun writeProperty(instance: Color, key: String, value: Any?) {
        errorBySetPropertyNotSupported(instance, key)
    }
}