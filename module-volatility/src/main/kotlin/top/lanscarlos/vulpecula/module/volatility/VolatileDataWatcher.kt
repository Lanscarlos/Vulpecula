package top.lanscarlos.vulpecula.module.volatility

import org.bukkit.entity.Entity
import org.bukkit.entity.Pose
import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
interface VolatileDataWatcher {

    fun getByteMetadata(entity: Entity, index: Int): Byte

    fun getIntMetadata(entity: Entity, index: Int): Int

    fun getFloatMetadata(entity: Entity, index: Int): Float

    fun getStringMetadata(entity: Entity, index: Int): Float

    fun createByteMetadata(index: Int, value: Byte): Any

    fun createIntMetadata(index: Int, value: Int): Any

    fun createFloatMetadata(index: Int, value: Float): Any

    fun createStringMetadata(index: Int, value: String): Any

    fun createPoseMetadata(index: Int, value: Pose): Any

    fun deconstruct(source: Pair<Int, Any>): Any

    companion object : VolatileDataWatcher by nmsProxy()

}