package top.lanscarlos.vulpecula.common.applicative

import taboolib.common.util.Location
import org.bukkit.entity.Entity
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Vector
import taboolib.platform.util.toProxyLocation

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 15:03
 */
object LocationApplicative : AbstractApplicative<Location>() {

    val REGEX_XYZ = "-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?(,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?)?".toRegex()

    val REGEX_WORLD_XYZ = "^[A-Za-z0-9_\\- \\u4e00-\\u9fa5]+,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?(,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?)?\$".toRegex()

    val REGEX_RELATIVE = "^(~?(?:[\\-+]?\\d+(?:\\.\\d+)?)?),(~?(?:[\\-+]?\\d+(?:\\.\\d+)?)?),(~?(?:[\\-+]?\\d+(?:\\.\\d+)?)?)\$".toRegex()

    override fun transfer(instance: Any, def: Location?): Location? {
        return when (instance) {
            is Location -> instance
            is org.bukkit.Location -> instance.toProxyLocation()
            is ProxyPlayer -> instance.location
            is Entity -> instance.location.toProxyLocation()
            is Vector -> Location(
                def?.world,
                instance.x,
                instance.y,
                instance.z,
                def?.yaw ?: 0.0f,
                def?.pitch ?: 0.0f
            )

            is org.bukkit.util.Vector -> Location(
                def?.world,
                instance.x,
                instance.y,
                instance.z,
                def?.yaw ?: 0.0f,
                def?.pitch ?: 0.0f
            )

            is String -> {

                // 匹配相对坐标 Example: 1,~,~+3.5
                REGEX_RELATIVE.matchEntire(instance)?.groupValues?.let { groupValues ->
                    return Location(
                        def?.world,
                        parseRelative(groupValues[1], def?.x ?: 0.0),
                        parseRelative(groupValues[2], def?.y ?: 0.0),
                        parseRelative(groupValues[3], def?.z ?: 0.0),
                    )
                }

                when {

                    instance.matches(REGEX_XYZ) -> {
                        /*
                        * x,y,z
                        * x,y,z,yaw,pitch
                        * */
                        val demand = instance.split(",")
                        Location(
                            def?.world,
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

                    else -> def
                }
            }

            else -> def
        }
    }

    override fun readProperty(instance: Location, key: String): Any? {
        return when (key) {
            "x" -> instance.x
            "y" -> instance.y
            "z" -> instance.z
            "yaw" -> instance.yaw
            "pitch" -> instance.pitch
            "world" -> instance.world
            "blockX" -> instance.blockX
            "blockY" -> instance.blockY
            "blockZ" -> instance.blockZ
            "length" -> instance.length()
            "lengthSquared" -> instance.lengthSquared()
            "direction" -> instance.direction
            "zero" -> instance.zero()
            "clone" -> instance.clone()
            else -> failedByGetPropertyNotSupported(instance, key)
        }
    }

    override fun writeProperty(instance: Location, key: String, value: Any?) {
        when (key) {
            "x" -> instance.x = value.applicativeDouble()
            "y" -> instance.y = value.applicativeDouble()
            "z" -> instance.z = value.applicativeDouble()
            "yaw" -> instance.yaw = value.applicativeFloat()
            "pitch" -> instance.pitch = value.applicativeFloat()
            "direction" -> instance.direction = value.applicativeVector()
            else -> failedBySetPropertyNotSupported(instance, key)
        }
    }

    private fun parseRelative(source: String, def: Double): Double {
        return if (source[0] == '~') {
            def + (source.substring(1).toDoubleOrNull() ?: 0.0)
        } else {
            source.toDoubleOrNull() ?: 0.0
        }
    }
}