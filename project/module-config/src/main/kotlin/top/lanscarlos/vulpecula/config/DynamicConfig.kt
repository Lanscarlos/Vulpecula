package top.lanscarlos.vulpecula.config

import java.io.File
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2023-08-25 00:30
 */
interface DynamicConfig {

    val file: File

    /**
     * 在重置所有节点数据之前执行
     * */
    fun onBeforeReload(runnable: Runnable)

    /**
     * 在重置所有节点数据之后执行
     * */
    fun onAfterReload(runnable: Runnable)

    /**
     * 获取数据
     * */
    operator fun get(path: String): Any?

    /**
     * 读取所有键
     * @param deep 是否深度读取
     * */
    fun readKeys(deep: Boolean): Set<String>

    /**
     * 读取所有键
     * @param path 路径
     * @param deep 是否深度读取
     * */
    fun readKeys(path: String, deep: Boolean): Set<String>

    fun read(path: String): DynamicSection<Any?>

    fun <T> read(path: String, transfer: Function<Any?, T>): DynamicSection<T>

    fun readBoolean(path: String, def: Boolean): DynamicSection<Boolean>

    fun readShort(path: String, def: Short): DynamicSection<Short>

    fun readInt(path: String, def: Int): DynamicSection<Int>

    fun readLong(path: String, def: Long): DynamicSection<Long>

    fun readFloat(path: String, def: Float): DynamicSection<Float>

    fun readDouble(path: String, def: Double): DynamicSection<Double>

    fun readString(path: String): DynamicSection<String?>

    fun readString(path: String, def: String): DynamicSection<String>

    fun readIntList(path: String, def: List<Int>): DynamicSection<List<Int>>

    fun readStringList(path: String, def: List<String>): DynamicSection<List<String>>

}