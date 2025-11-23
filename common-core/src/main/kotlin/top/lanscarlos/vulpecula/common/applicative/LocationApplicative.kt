package top.lanscarlos.vulpecula.common.applicative

import taboolib.common.util.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Vector
import taboolib.platform.util.toBukkitLocation
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.vulpecula.common.applicative.exception.ValueConversionException
import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 15:03
 */
object LocationApplicative : AbstractApplicative<Location>(Location::class.java) {

    private val REGEX_XYZ = "-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?(,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?)?".toRegex()

    private val REGEX_WORLD_XYZ = "^[A-Za-z0-9_\\- \\u4e00-\\u9fa5]+,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?(,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?)?\$".toRegex()

    private val REGEX_RELATIVE = "^(~?(?:[\\-+]?\\d+(?:\\.\\d+)?)?),(~?(?:[\\-+]?\\d+(?:\\.\\d+)?)?),(~?(?:[\\-+]?\\d+(?:\\.\\d+)?)?)\$".toRegex()

    override fun convertOrThrow(instance: Any): Location {
        return when (instance) {
            is Location -> instance
            is org.bukkit.Location -> instance.toProxyLocation()
            is ProxyPlayer -> instance.location
            is Entity -> instance.location.toProxyLocation()
            is Vector -> Location(null, instance.x, instance.y, instance.z)
            is org.bukkit.util.Vector -> Location(null, instance.x, instance.y, instance.z)
            is String -> convert(instance)
            else -> throw TypeConversionException(instance::class.java, String::class.java)
        }
    }

    private fun convert(instance: String): Location {
        return when {
            instance.matches(REGEX_RELATIVE) -> {
                // 匹配相对坐标 Example: 1,~,~+3.5
                val groupValues = REGEX_RELATIVE.matchEntire(instance)!!.groupValues
                Location(
                    null,
                    parseRelative(groupValues[1]),
                    parseRelative(groupValues[2]),
                    parseRelative(groupValues[3])
                )
            }
            instance.matches(REGEX_XYZ) -> {
                /*
                * x,y,z
                * x,y,z,yaw,pitch
                * */
                val demand = instance.split(",")
                Location(
                    null,
                    demand[0].toDouble(),
                    demand[1].toDouble(),
                    demand[2].toDouble(),
                    demand.getOrNull(3)?.toFloatOrNull() ?: 0f,
                    demand.getOrNull(4)?.toFloatOrNull() ?: 0f
                )
            }
            instance.matches(REGEX_WORLD_XYZ) -> {
                /*
                * world,x,y,z
                * world,x,y,z,yaw,pitch
                * */
                val demand = instance.split(",")
                Location(
                    demand[0],
                    demand[1].toDouble(),
                    demand[2].toDouble(),
                    demand[3].toDouble(),
                    demand.getOrNull(4)?.toFloatOrNull() ?: 0f,
                    demand.getOrNull(5)?.toFloatOrNull() ?: 0f
                )
            }
            else -> throw ValueConversionException(instance, Player::class.java)
        }
    }


    private fun parseRelative(source: String): Double {
        return if (source[0] == '~') {
            0.0 + (source.substring(1).toDoubleOrNull() ?: 0.0)
        } else {
            source.toDoubleOrNull() ?: 0.0
        }
    }
}

object BukkitLocationApplicative : AbstractApplicative<org.bukkit.Location>(org.bukkit.Location::class.java) {

    override fun convertOrThrow(instance: Any): org.bukkit.Location {
        return LocationApplicative.convertOrThrow(instance).toBukkitLocation()
    }

}