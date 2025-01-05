package top.lanscarlos.vulpecula.common.config

import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-01-04 14:03
 */
interface ConfigSection {

    operator fun contains(key: String): Boolean

    operator fun get(key: String): Any?

    operator fun set(key: String, value: Any?)

    fun remove(key: String)

    fun getBoolean(key: String): Boolean?

    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    fun getInt(key: String): Int?

    fun getInt(key: String, defaultValue: Int): Int

    fun getLong(key: String): Long?

    fun getLong(key: String, defaultValue: Long): Long

    fun getFloat(key: String): Float?

    fun getFloat(key: String, defaultValue: Float): Float

    fun getDouble(key: String): Double?

    fun getDouble(key: String, defaultValue: Double): Double

    fun getString(key: String): String?

    fun getString(key: String, defaultValue: String): String

    fun getIntList(key: String): List<Int>?

    fun getIntList(key: String, defaultValue: List<Int>): List<Int>

    fun getLongList(key: String): List<Long>?

    fun getLongList(key: String, defaultValue: List<Long>): List<Long>

    fun getFloatList(key: String): List<Float>?

    fun getFloatList(key: String, defaultValue: List<Float>): List<Float>

    fun getDoubleList(key: String): List<Double>?

    fun getDoubleList(key: String, defaultValue: List<Double>): List<Double>

    fun getStringList(key: String): List<String>?

    fun getStringList(key: String, defaultValue: List<String>): List<String>

    fun getList(key: String): List<*>?

    fun getList(key: String, defaultValue: List<*>): List<*>

    fun <T> getList(key: String, transfer: Function<Any?, T>): List<T>?

    fun <T> getList(key: String, defaultValue: List<T>, transfer: Function<Any?, T>): List<T>

    fun <T> read(key: String, transfer: Function<Any?, T>): ConfigNode<T>

    fun readBoolean(key: String): ConfigNode<Boolean>

    fun readBoolean(key: String, defaultValue: Boolean): ConfigNode<Boolean>

    fun readInt(key: String): ConfigNode<Int>

    fun readInt(key: String, defaultValue: Int): ConfigNode<Int>

    fun readLong(key: String): ConfigNode<Long>

    fun readLong(key: String, defaultValue: Long): ConfigNode<Long>

    fun readFloat(key: String): ConfigNode<Float>

    fun readFloat(key: String, defaultValue: Float): ConfigNode<Float>

    fun readDouble(key: String): ConfigNode<Double>

    fun readDouble(key: String, defaultValue: Double): ConfigNode<Double>

    fun readString(key: String): ConfigNode<String>

    fun readString(key: String, defaultValue: String): ConfigNode<String>

    fun readIntList(key: String): ConfigNode<List<Int>>

    fun readIntList(key: String, defaultValue: List<Int>): ConfigNode<List<Int>>

    fun readLongList(key: String): ConfigNode<List<Long>>

    fun readLongList(key: String, defaultValue: List<Long>): ConfigNode<List<Long>>

    fun readFloatList(key: String): ConfigNode<List<Float>>

    fun readFloatList(key: String, defaultValue: List<Float>): ConfigNode<List<Float>>

    fun readDoubleList(key: String): ConfigNode<List<Double>>

    fun readDoubleList(key: String, defaultValue: List<Double>): ConfigNode<List<Double>>

    fun readStringList(key: String): ConfigNode<List<String>>

    fun readStringList(key: String, defaultValue: List<String>): ConfigNode<List<String>>

    fun readList(key: String): ConfigNode<List<*>>

    fun readList(key: String, defaultValue: List<*>): ConfigNode<List<*>>

    fun <T> readList(key: String, transfer: Function<Any?, T>): ConfigNode<List<T>>

    fun <T> readList(key: String, defaultValue: List<T>, transfer: Function<Any?, T>): ConfigNode<List<T>>

}