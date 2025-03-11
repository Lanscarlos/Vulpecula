package top.lanscarlos.vulpecula.common.config

import top.lanscarlos.vulpecula.common.applicative.*
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2024-12-16 13:52
 */
abstract class AbstractConfigSection : ConfigSection {

    override fun getBoolean(key: String): Boolean? {
        return get(key)?.let(BooleanApplicative::convertOrNull)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getBoolean(key) ?: defaultValue
    }

    override fun getInt(key: String): Int? {
        return get(key)?.let(IntApplicative::convertOrNull)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return getInt(key) ?: defaultValue
    }

    override fun getLong(key: String): Long? {
        return get(key)?.let(LongApplicative::convertOrNull)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return getLong(key) ?: defaultValue
    }

    override fun getFloat(key: String): Float? {
        return get(key)?.let(FloatApplicative::convertOrNull)
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return getFloat(key) ?: defaultValue
    }

    override fun getDouble(key: String): Double? {
        return get(key)?.let(DoubleApplicative::convertOrNull)
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return getDouble(key) ?: defaultValue
    }

    override fun getString(key: String): String? {
        return get(key)?.let(StringApplicative::convertOrNull)
    }

    override fun getString(key: String, defaultValue: String): String {
        return getString(key) ?: defaultValue
    }

    override fun getIntList(key: String): List<Int>? {
        return getList(key, IntApplicative::convertOrThrow)
    }

    override fun getIntList(key: String, defaultValue: List<Int>): List<Int> {
        return getIntList(key) ?: defaultValue
    }

    override fun getLongList(key: String): List<Long>? {
        return getList(key, LongApplicative::convertOrThrow)
    }

    override fun getLongList(key: String, defaultValue: List<Long>): List<Long> {
        return getLongList(key) ?: defaultValue
    }

    override fun getFloatList(key: String): List<Float>? {
        return getList(key, FloatApplicative::convertOrThrow)
    }

    override fun getFloatList(key: String, defaultValue: List<Float>): List<Float> {
        return getFloatList(key) ?: defaultValue
    }

    override fun getDoubleList(key: String): List<Double>? {
        return getList(key, DoubleApplicative::convertOrThrow)
    }

    override fun getDoubleList(key: String, defaultValue: List<Double>): List<Double> {
        return getDoubleList(key) ?: defaultValue
    }

    override fun getStringList(key: String): List<String>? {
        return getList(key, StringApplicative::convertOrThrow)
    }

    override fun getStringList(key: String, defaultValue: List<String>): List<String> {
        return getStringList(key) ?: defaultValue
    }

    override fun getList(key: String): List<*>? {
        return get(key)?.let(ListApplicative::convertOrNull)
    }

    override fun getList(key: String, defaultValue: List<*>): List<*> {
        return getList(key) ?: defaultValue
    }

    override fun <T> getList(key: String, transfer: Function<Any?, T>): List<T>? {
        return get(key)?.let(ListApplicative::convertOrNull)?.map(transfer::apply)
    }

    override fun <T> getList(key: String, defaultValue: List<T>, transfer: Function<Any?, T>): List<T> {
        return getList(key, transfer) ?: defaultValue
    }

}