package top.lanscarlos.vulpecula.applicative

import java.awt.Color

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 14:57
 */
class ColorApplicative(source: Any) : AbstractApplicative<Color>(source) {

    override fun transfer(source: Any, def: Color?): Color? {
        return when (source) {
            is Color -> source
            is org.bukkit.Color -> Color(source.red, source.green, source.blue)
            is String -> {
                when {
                    source.startsWith('#') && source.matches(PATTERN_HEX) -> {
                        // hex
                        Color.decode(source)
                    }
                    source.matches(PATTERN_RGB) -> {
                        val demand = source.split("-").map { it.toInt().coerceIn(0, 255) }
                        if (demand.size == 4) {
                            // r-g-b-a
                            Color(demand[0], demand[1], demand[2], demand[3])
                        } else {
                            Color(demand[0], demand[1], demand[2], def?.alpha ?: 255)
                        }
                    }
                    else -> {
                        val rgb = source.toIntOrNull() ?: return def
                        Color(rgb)
                    }
                }
            }

            else -> def
        }
    }

    companion object {

        val PATTERN_HEX = "^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})\$".toRegex()
        val PATTERN_RGB = "^\\d+-\\d+-\\d+(-\\d+)?\$".toRegex()

        fun Any.applicativeColor() = ColorApplicative(this)
    }
}