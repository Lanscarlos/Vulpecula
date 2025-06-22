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
object VectorApplicative : AbstractApplicative<Vector>() {

    val REGEX_NUMBER = "-?\\d+(\\.\\d+)?".toRegex()

    val REGEX_XYZ = "^-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?\$".toRegex()

    override fun transfer(instance: Any, def: Vector?): Vector? {
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

                    else -> def
                }
            }
            else -> def
        }
    }

    override fun readProperty(instance: Vector, key: String): Any {
        return when (key) {
            "x" -> instance.x
            "y" -> instance.y
            "z" -> instance.z
            "blockX" -> instance.blockX
            "blockY" -> instance.blockY
            "blockZ" -> instance.blockZ
            "length" -> instance.length()
            "lengthSquared" -> instance.lengthSquared()
            "normalize" -> instance.normalize()
            "isNormalized" -> instance.isNormalized
            "zero" -> instance.zero()
            "clone" -> instance.clone()
            else -> failedByGetPropertyNotSupported(instance, key)
        }
    }

    override fun writeProperty(instance: Vector, key: String, value: Any?) {
        when (key) {
            "x" -> instance.x = value.applicativeDouble()
            "y" -> instance.y = value.applicativeDouble()
            "z" -> instance.z = value.applicativeDouble()
            else -> failedBySetPropertyNotSupported(instance, key)
        }
    }
}