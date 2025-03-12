package top.lanscarlos.vulpecula.dispatcher.condition

import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import taboolib.common.util.Vector
import taboolib.common5.cdouble
import taboolib.common5.cint
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.*
import kotlin.math.max
import kotlin.math.min

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.condition
 *
 * @author Lanscarlos
 * @since 2025-03-10 13:47
 */
class PlayerCondition(config: ConfigurationSection) : Condition<Player> {

    /**
     * 是否是OP
     * */
    val op by config.read("op").booleanOrNull()

    /**
     * 权限检查
     * */
    val permission by config.read("permission").stringListOrNull()

    /**
     * 世界检查
     * */
    val world by config.read("world").stringListOrNull()

    /**
     * 区域检查
     * */
    val regionIn by config.read("area-in", "region-in").convert(::parseRegion)

    /**
     * 区域检查
     * */
    val regionOut by config.read("area-out", "region-out").convert(::parseRegion)

    override fun check(input: Player): Boolean {
        if (op != null) {
            if (op!! && !input.isOp) {
                return false
            }
        }
        if (permission != null && permission!!.isNotEmpty()) {
            if (permission!!.any { !input.hasPermission(it) }) {
                return false
            }
        }
        if (world != null && world!!.isNotEmpty()) {
            if (!world!!.contains(input.world.name)) {
                return false
            }
        }
        if (regionIn != null) {
            val location = input.location
            if (!regionIn!!.contains(location.x, location.y, location.z)) {
                return false
            }
        }
        if (regionOut != null) {
            val location = input.location
            if (regionOut!!.contains(location.x, location.y, location.z)) {
                return false
            }
        }
        return true
    }

    /**
     * 解析区域
     *
     * @param value 区域数据
     * @return 碰撞箱
     * */
    private fun parseRegion(value: Any?): BoundingBox? {
        if (value == null) {
            return null
        }
        val (minLoc, maxLoc) = when (value) {
            is String -> {
                // 坐标 x,y,z~x,y,z
                require(value.matches(Regex("-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?~-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?"))) { "Region string must be x,y,z~x,y,z." }
                val data = value.split("~", ",").map { it.cdouble }
                val vec1 = Vector(data[0], data[1], data[2])
                val vec2 = Vector(data[3], data[4], data[5])
                vec1 to vec2
            }
            is List<*> -> {
                // 坐标 [x,y,z,x,y,z]
                require(value.size == 6) { "Region list must be 6 elements." }
                val data = value.map { it.cdouble }
                val vec1 = Vector(data[0], data[1], data[2])
                val vec2 = Vector(data[3], data[4], data[5])
                vec1 to vec2
            }
            is Map<*, *> -> {
                // 坐标 { pos1: [x,y,z], pos2: [x,y,z] }
                require(value.containsKey("pos1") && value.containsKey("pos2")) { "Region map must contains key pos1 and pos2." }
                val pos1 = value["pos1"] as List<*>
                val pos2 = value["pos2"] as List<*>
                require(pos1.size == 3 && pos2.size == 3) { "Region pos1 and pos2 must be 3 elements." }
                val vec1 = Vector(pos1[0].cdouble, pos1[1].cdouble, pos1[2].cdouble)
                val vec2 = Vector(pos2[0].cdouble, pos2[1].cdouble, pos2[2].cdouble)
                vec1 to vec2
            }
            else -> {
                error("Region must be string, list or map.")
            }
        }
        // 调整坐标
        minLoc.x = min(minLoc.x, maxLoc.x)
        minLoc.y = min(minLoc.y, maxLoc.y)
        minLoc.z = min(minLoc.z, maxLoc.z)
        maxLoc.x = max(minLoc.x, maxLoc.x)
        maxLoc.y = max(minLoc.y, maxLoc.y)
        maxLoc.z = max(minLoc.z, maxLoc.z)
        return BoundingBox(minLoc.x, minLoc.y, minLoc.z, maxLoc.x, maxLoc.y, maxLoc.z)
    }

}