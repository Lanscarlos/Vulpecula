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
        return get(key)?.let(BooleanApplicative::convert)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getBoolean(key) ?: defaultValue
    }

    override fun getInt(key: String): Int? {
        return get(key)?.let(IntApplicative::convert)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return getInt(key) ?: defaultValue
    }

    override fun getLong(key: String): Long? {
        return get(key)?.let(LongApplicative::convert)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return getLong(key) ?: defaultValue
    }

    override fun getFloat(key: String): Float? {
        return get(key)?.let(FloatApplicative::convert)
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return getFloat(key) ?: defaultValue
    }

    override fun getDouble(key: String): Double? {
        return get(key)?.let(DoubleApplicative::convert)
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return getDouble(key) ?: defaultValue
    }

    override fun getString(key: String): String? {
        return get(key)?.let(StringApplicative::convert)
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
        return get(key)?.let(ListApplicative::convert)
    }

    override fun getList(key: String, defaultValue: List<*>): List<*> {
        return getList(key) ?: defaultValue
    }

    override fun <T> getList(key: String, transfer: Function<Any?, T>): List<T>? {
        return get(key)?.let(ListApplicative::convert)?.map(transfer::apply)
    }

    override fun <T> getList(key: String, defaultValue: List<T>, transfer: Function<Any?, T>): List<T> {
        return getList(key, transfer) ?: defaultValue
    }

    override fun readBoolean(key: String): ConfigNode<Boolean> {
        return readBoolean(key, false)
    }

    override fun readBoolean(key: String, defaultValue: Boolean): ConfigNode<Boolean> {
        return read(key) { BooleanApplicative.convert(it) ?: defaultValue }
    }

    override fun readInt(key: String): ConfigNode<Int> {
        return read(key, IntApplicative::convertOrThrow)
    }

    override fun readInt(key: String, defaultValue: Int): ConfigNode<Int> {
        return read(key) { IntApplicative.convert(it) ?: defaultValue }
    }

    override fun readLong(key: String): ConfigNode<Long> {
        return read(key, LongApplicative::convertOrThrow)
    }

    override fun readLong(key: String, defaultValue: Long): ConfigNode<Long> {
        return read(key) { LongApplicative.convert(it) ?: defaultValue }
    }

    override fun readFloat(key: String): ConfigNode<Float> {
        return read(key, FloatApplicative::convertOrThrow)
    }

    override fun readFloat(key: String, defaultValue: Float): ConfigNode<Float> {
        return read(key) { FloatApplicative.convert(it) ?: defaultValue }
    }

    override fun readDouble(key: String): ConfigNode<Double> {
        return read(key, DoubleApplicative::convertOrThrow)
    }

    override fun readDouble(key: String, defaultValue: Double): ConfigNode<Double> {
        return read(key) { DoubleApplicative.convert(it) ?: defaultValue }
    }

    override fun readString(key: String): ConfigNode<String> {
        return read(key, StringApplicative::convertOrThrow)
    }

    override fun readString(key: String, defaultValue: String): ConfigNode<String> {
        return read(key) { StringApplicative.convert(it) ?: defaultValue }
    }

    override fun readIntList(key: String): ConfigNode<List<Int>> {
        return readList(key, IntApplicative::convertOrThrow)
    }

    override fun readIntList(key: String, defaultValue: List<Int>): ConfigNode<List<Int>> {
        return readList(key, defaultValue, IntApplicative::convertOrThrow)
    }

    override fun readLongList(key: String): ConfigNode<List<Long>> {
        return readList(key, LongApplicative::convertOrThrow)
    }

    override fun readLongList(key: String, defaultValue: List<Long>): ConfigNode<List<Long>> {
        return readList(key, defaultValue, LongApplicative::convertOrThrow)
    }

    override fun readFloatList(key: String): ConfigNode<List<Float>> {
        return readList(key, FloatApplicative::convertOrThrow)
    }

    override fun readFloatList(key: String, defaultValue: List<Float>): ConfigNode<List<Float>> {
        return readList(key, defaultValue, FloatApplicative::convertOrThrow)
    }

    override fun readDoubleList(key: String): ConfigNode<List<Double>> {
        return readList(key, DoubleApplicative::convertOrThrow)
    }

    override fun readDoubleList(key: String, defaultValue: List<Double>): ConfigNode<List<Double>> {
        return readList(key, defaultValue, DoubleApplicative::convertOrThrow)
    }

    override fun readStringList(key: String): ConfigNode<List<String>> {
        return readList(key, StringApplicative::convertOrThrow)
    }

    override fun readStringList(key: String, defaultValue: List<String>): ConfigNode<List<String>> {
        return readList(key, defaultValue, StringApplicative::convertOrThrow)
    }

    override fun readList(key: String): ConfigNode<List<*>> {
        return read(key) { it?.let(ListApplicative::convert) ?: emptyList<Any?>() }
    }

    override fun readList(key: String, defaultValue: List<*>): ConfigNode<List<*>> {
        return read(key) { it?.let(ListApplicative::convert) ?: defaultValue }
    }

    override fun <T> readList(key: String, transfer: Function<Any?, T>): ConfigNode<List<T>> {
        return read(key) { it?.let(ListApplicative::convert)?.map(transfer::apply) ?: emptyList() }
    }

    override fun <T> readList(key: String, defaultValue: List<T>, transfer: Function<Any?, T>): ConfigNode<List<T>> {
        return read(key) { it?.let(ListApplicative::convert)?.map(transfer::apply) ?: defaultValue }
    }

}