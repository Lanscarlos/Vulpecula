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

    fun createPoseMetadata(index: Int, value: VolatilePose): Any

    fun deconstruct(source: Pair<Int, Any>): Any {
        return when (val value = source.second) {
            is Byte -> createByteMetadata(source.first, value)
            is Int -> createIntMetadata(source.first, value)
            is Float -> createFloatMetadata(source.first, value)
            is String -> createStringMetadata(source.first, value)
            is VolatilePose -> createPoseMetadata(source.first, value)
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.java.name}")
        }
    }

    companion object : VolatileMetadata by nmsProxy("${VolatileMetadata::class.java.`package`.name}.DefaultVolatileMetadata")

}