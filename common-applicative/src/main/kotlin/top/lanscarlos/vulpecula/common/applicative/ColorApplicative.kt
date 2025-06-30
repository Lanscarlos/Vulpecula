package top.lanscarlos.vulpecula.common.applicative

import top.lanscarlos.vulpecula.common.applicative.exception.ValueConversionException
import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException
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

    private val bukkitColors = mapOf(
        "BLACK" to org.bukkit.Color.BLACK, // &0
        "NAVY" to org.bukkit.Color.NAVY, // &1
        "GREEN" to org.bukkit.Color.GREEN, // &2
        "TEAL" to org.bukkit.Color.TEAL, // &3
        "MAROON" to org.bukkit.Color.MAROON, // &4
        "PURPLE" to org.bukkit.Color.PURPLE, // &5
        "ORANGE" to org.bukkit.Color.ORANGE, // &6
        "SILVER" to org.bukkit.Color.SILVER, // &7
        "GRAY" to org.bukkit.Color.GRAY, // &8
        "BLUE" to org.bukkit.Color.BLUE, // &9
        "LIME" to org.bukkit.Color.LIME, // &a
        "AQUA" to org.bukkit.Color.AQUA, // &b
        "YELLOW" to org.bukkit.Color.YELLOW, // &e
        "RED" to org.bukkit.Color.RED, // &c
        "FUCHSIA" to org.bukkit.Color.FUCHSIA, // &d
        "WHITE" to org.bukkit.Color.WHITE, // &f
        "OLIVE" to org.bukkit.Color.OLIVE
    )

    override fun convertOrThrow(instance: Any): Color {
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
                    bukkitColors.contains(instance.uppercase()) -> {
                        val bukkitColor = bukkitColors[instance.uppercase()]!!
                        try {
                            Color(bukkitColor.red, bukkitColor.green, bukkitColor.blue, bukkitColor.alpha)
                        } catch (_: NoSuchMethodError) {
                            Color(bukkitColor.red, bukkitColor.green, bukkitColor.blue)
                        }
                    }
                    else -> {
                        val rgb = instance.toIntOrNull() ?: throw ValueConversionException(instance, Color::class.java)
                        Color(rgb)
                    }
                }
            }
            else -> throw TypeConversionException(instance::class.java, Color::class.java)
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