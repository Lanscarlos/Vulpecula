package top.lanscarlos.vulpecula.config

import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2023-08-25 00:30
 */
interface DynamicConfig {

    /**
     * 获取数据
     * */
    operator fun get(path: String): Any?

    fun read(path: String): DynamicSection<Any?>

    fun <T> read(path: String, transfer: Function<Any?, T>): DynamicSection<T>

    fun readBoolean(path: String, def: Boolean): DynamicSection<Boolean>

    fun readShort(path: String, def: Short): DynamicSection<Short>

    fun readInt(path: String, def: Int): DynamicSection<Int>

    fun readLong(path: String, def: Long): DynamicSection<Long>

    fun readFloat(path: String, def: Float): DynamicSection<Float>

    fun readDouble(path: String, def: Double): DynamicSection<Double>

    fun readString(path: String, def: String): DynamicSection<String>

    fun readIntList(path: String, def: List<Int>): DynamicSection<List<Int>>

    fun readStringList(path: String, def: List<String>): DynamicSection<List<String>>

}