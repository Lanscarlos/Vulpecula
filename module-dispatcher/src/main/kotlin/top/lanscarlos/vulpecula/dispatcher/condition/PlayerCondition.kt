package top.lanscarlos.vulpecula.dispatcher.condition

import org.bukkit.entity.Player
import top.lanscarlos.vulpecula.common.config.ConfigSection
import top.lanscarlos.vulpecula.common.livedata.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.condition
 *
 * @author Lanscarlos
 * @since 2025-03-10 13:47
 */
class PlayerCondition(config: ConfigSection) : Condition<Player> {

    /**
     * 权限检查
     * */
    val permission by config.read("permission").convertToStringList()

    /**
     * 是否是OP
     * */
    val op by config.read("op").convertToBoolean(false)

    /**
     * 世界检查
     * */
    val world by config.read("world").convertToStringList()

    /**
     * 区域检查
     * */
    val region by config.read("area", "region").convertToNormalizeMap().convert {
    }

    override fun check(input: Player): Boolean {
        TODO("Not yet implemented")
    }
}