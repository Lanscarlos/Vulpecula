package top.lanscarlos.vulpecula.common.applicative

import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.vulpecula.common.applicative.exception.ValueConversionException
import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 14:18
 */
object VectorApplicative : AbstractApplicative<Vector>(Vector::class.java) {

    private val REGEX_NUMBER = "-?\\d+(\\.\\d+)?".toRegex()

    private val REGEX_XYZ = "^-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?\$".toRegex()

    override fun convertOrThrow(instance: Any): Vector {
        return when (instance) {
            is Vector -> instance
            is org.bukkit.util.Vector -> Vector(instance.x, instance.y, instance.z)
            is Location -> instance.direction
            is org.bukkit.Location -> instance.toProxyLocation().direction
            "x" -> Vector(1.0, 0.0, 0.0)
            "y" -> Vector(0.0, 1.0, 0.0)
            "z" -> Vector(0.0, 0.0, 1.0)
            is String -> {
                when {
                    instance.matches(REGEX_NUMBER) -> {
                        // 数字
                        val number = instance.toDouble()
                        Vector(number, number, number)
                    }
                    instance.matches(REGEX_XYZ) -> {
                        // x,y,z
                        val demand = instance.split(",").map { it.toDouble() }
                        Vector(demand[0], demand[1], demand[2])
                    }
                    else -> throw ValueConversionException(instance, Vector::class.java)
                }
            }
            else -> throw TypeConversionException(instance, Vector::class.java)
        }
    }

}

object BukkitVectorApplicative : AbstractApplicative<org.bukkit.util.Vector>(org.bukkit.util.Vector::class.java) {

    override fun convertOrThrow(instance: Any): org.bukkit.util.Vector {
        return VectorApplicative.convertOrThrow(instance).let { org.bukkit.util.Vector(it.x, it.y, it.z) }
    }

}