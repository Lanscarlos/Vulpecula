package top.lanscarlos.vulpecula.module.volatility

import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
interface VolatileDataWatcher {

    fun createByteMetadata(index: Int, value: Byte): Any

    fun createIntMetadata(index: Int, value: Int): Any

    fun createFloatMetadata(index: Int, value: Float): Any

    fun createStringMetadata(index: Int, value: String): Any

    fun deconstruct(source: Pair<Int, Any>): Any

    companion object : VolatileDataWatcher by nmsProxy("${VolatileDataWatcher::class.java.`package`.name}.Default${VolatileDataWatcher::class.java.simpleName}")

}