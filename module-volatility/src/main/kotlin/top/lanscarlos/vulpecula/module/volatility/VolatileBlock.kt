package top.lanscarlos.vulpecula.module.volatility

import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/9 10:14
 */
interface VolatileBlock {

    fun sendBlockChange(viewer: Player, location: Location, data: BlockData)

    fun createBlockChange(location: Location, data: BlockData): Any

    fun createMultiBlockChange(data: List<Pair<Location, BlockData>>): Any

    companion object : VolatileBlock by nmsProxy()

}