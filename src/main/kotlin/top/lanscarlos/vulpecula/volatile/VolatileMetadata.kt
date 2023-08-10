package top.lanscarlos.vulpecula.volatile

import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.volatile
 *
 * @author Lanscarlos
 * @since 2023-08-10 11:09
 */
interface VolatileMetadata {

    fun createByteMetadata(index: Int, value: Byte): Any

    fun createIntMetadata(index: Int, value: Int): Any

    fun createFloatMetadata(index: Int, value: Float): Any

    fun createStringMetadata(index: Int, value: String): Any

    fun deconstruct(source: Pair<Int, Any>): Any

    companion object : VolatileMetadata by nmsProxy("${VolatileMetadata::class.java.`package`.name}.DefaultVolatileMetadata")

}