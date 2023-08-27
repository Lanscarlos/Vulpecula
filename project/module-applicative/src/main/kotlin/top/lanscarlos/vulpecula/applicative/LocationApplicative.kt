package top.lanscarlos.vulpecula.applicative

import taboolib.common.util.Location
import org.bukkit.entity.Entity
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Vector
import taboolib.platform.util.toProxyLocation

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 15:03
 */
class LocationApplicative(source: Any) : AbstractApplicative<Location>(source) {

    override fun transfer(source: Any, def: Location?): Location? {
        return when (source) {
            is Location -> source
            is org.bukkit.Location -> source.toProxyLocation()
            is ProxyPlayer -> source.location
            is Entity -> source.location.toProxyLocation()
            is Vector -> Location(
                def?.world,
                source.x,
                source.y,
                source.z,
                def?.yaw ?: 0.0f,
                def?.pitch ?: 0.0f
            )

            is org.bukkit.util.Vector -> Location(
                def?.world,
                source.x,
                source.y,
                source.z,
                def?.yaw ?: 0.0f,
                def?.pitch ?: 0.0f
            )

            is String -> {

                // 匹配相对坐标 Example: 1,~,~+3.5
                PATTERN_RELATIVE.matchEntire(source)?.groupValues?.let { groupValues ->
                    return Location(
                        def?.world,
                        parseRelative(groupValues[1], def?.x ?: 0.0),
                        parseRelative(groupValues[2], def?.y ?: 0.0),
                        parseRelative(groupValues[3], def?.z ?: 0.0),
                    )
                }

                when {

                    source.matches(PATTERN_XYZ) -> {
                        /*
                        * x,y,z
                        * x,y,z,yaw,pitch
                        * */
                        val demand = source.split(",")
                        Location(
                            def?.world,
                            demand[0].toDouble(),
                            demand[1].toDouble(),
                            demand[2].toDouble(),
                            demand.getOrNull(3)?.toFloatOrNull() ?: 0f,
                            demand.getOrNull(4)?.toFloatOrNull() ?: 0f
                        )
                    }

                    source.matches(PATTERN_WORLD_XYZ) -> {
                        /*
                        * world,x,y,z
                        * world,x,y,z,yaw,pitch
                        * */
                        val demand = source.split(",")
                        Location(
                            demand[0],
                            demand[1].toDouble(),
                            demand[2].toDouble(),
                            demand[3].toDouble(),
                            demand.getOrNull(4)?.toFloatOrNull() ?: 0f,
                            demand.getOrNull(5)?.toFloatOrNull() ?: 0f
                        )
                    }

                    else -> def
                }
            }

            else -> def
        }
    }

    fun parseRelative(source: String, def: Double): Double {
        return if (source[0] == '~') {
            return def + (source.substring(1).toDoubleOrNull() ?: 0.0)
        } else {
            return source.toDoubleOrNull() ?: 0.0
        }
    }

    companion object {

        val PATTERN_XYZ =
            "-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?(,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?)?".toRegex()

        val PATTERN_WORLD_XYZ =
            "^[A-Za-z0-9_\\- \\u4e00-\\u9fa5]+,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?(,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?)?\$".toRegex()

        val PATTERN_RELATIVE =
            "^(~?(?:[\\-+]?\\d+(?:\\.\\d+)?)?),(~?(?:[\\-+]?\\d+(?:\\.\\d+)?)?),(~?(?:[\\-+]?\\d+(?:\\.\\d+)?)?)\$".toRegex()

        fun Any.applicativeLocation() = LocationApplicative(this)
    }
}