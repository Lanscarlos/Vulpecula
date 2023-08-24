package top.lanscarlos.vulpecula.applicative

import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.platform.util.toProxyLocation

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 14:18
 */
class VectorApplicative(source: Any) : AbstractApplicative<Vector>(source) {

    override fun transfer(source: Any, def: Vector?): Vector? {
        return when (source) {
            is Vector -> source
            is org.bukkit.util.Vector -> Vector(source.x, source.y, source.z)
            is Location -> source.direction
            is org.bukkit.Location -> source.toProxyLocation().direction
            "x" -> Vector(1.0, 0.0, 0.0)
            "y" -> Vector(0.0, 1.0, 0.0)
            "z" -> Vector(0.0, 0.0, 1.0)
            is String -> {
                when {
                    source.matches(PATTERN_NUMBER) -> {
                        // 数字
                        val number = source.toDouble()
                        Vector(number, number, number)
                    }

                    source.matches(PATTERN_XYZ) -> {
                        // x,y,z
                        val demand = source.split(",").map { it.toDouble() }
                        Vector(demand[0], demand[1], demand[2])
                    }

                    else -> def
                }
            }
            else -> def
        }
    }

    companion object {

        val PATTERN_NUMBER = "-?\\d+(\\.\\d+)?".toRegex()
        val PATTERN_XYZ = "^-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?\$".toRegex()

        fun Any.applicativeVector() = VectorApplicative(this)
    }
}