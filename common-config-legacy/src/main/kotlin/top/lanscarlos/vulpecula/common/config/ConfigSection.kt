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

    val path: String

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

    fun getSection(key: String): ConfigSection

    fun read(vararg key: String): ConfigNode

}