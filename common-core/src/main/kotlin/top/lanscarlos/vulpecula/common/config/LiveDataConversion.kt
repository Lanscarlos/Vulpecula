package top.lanscarlos.vulpecula.common.config

import top.lanscarlos.vulpecula.common.applicative.*
import java.util.function.Function
import kotlin.collections.map

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-03-10 19:04
 */

fun <T> LiveData<T>.exceptionally(exceptionally: Function<Exception, T>): LiveData<T> {
    return ExceptionalLiveData(this, exceptionally)
}

fun <T, R> LiveData<T>.convert(transformer: Function<T, R>): LiveData<R> {
    return LiveDataTransformer(this, transformer)
}

fun <T> LiveData<T?>.default(defaultValue: T): LiveData<T> {
    return LiveDataTransformer(this) { it ?: defaultValue }
}

fun <T> LiveData<T>.booleanOrNull(): LiveData<Boolean?> {
    return LiveDataTransformer(this, BooleanApplicative::convertOrNull)
}

fun <T> LiveData<T>.boolean(): LiveData<Boolean> {
    return LiveDataTransformer(this, BooleanApplicative::convert)
}

fun <T> LiveData<T>.boolean(defaultValue: Boolean): LiveData<Boolean> {
    return LiveDataTransformer(this) {
        if (it == null) {
            return@LiveDataTransformer defaultValue
        }
        BooleanApplicative.convert(it)
    }
}

fun <T> LiveData<T>.intOrNull(): LiveData<Int?> {
    return LiveDataTransformer(this, IntApplicative::convertOrNull)
}

fun <T> LiveData<T>.int(): LiveData<Int> {
    return LiveDataTransformer(this, IntApplicative::convert)
}

fun <T> LiveData<T>.int(defaultValue: Int): LiveData<Int> {
    return LiveDataTransformer(this) {
        if (it == null) {
            return@LiveDataTransformer defaultValue
        }
        IntApplicative.convert(it)
    }
}

fun <T> LiveData<T>.longOrNull(): LiveData<Long?> {
    return LiveDataTransformer(this, LongApplicative::convertOrNull)
}

fun <T> LiveData<T>.long(): LiveData<Long> {
    return LiveDataTransformer(this, LongApplicative::convert)
}

fun <T> LiveData<T>.long(defaultValue: Long): LiveData<Long> {
    return LiveDataTransformer(this) {
        if (it == null) {
            return@LiveDataTransformer defaultValue
        }
        LongApplicative.convert(it)
    }
}

fun <T> LiveData<T>.floatOrNull(): LiveData<Float?> {
    return LiveDataTransformer(this, FloatApplicative::convertOrNull)
}

fun <T> LiveData<T>.float(): LiveData<Float> {
    return LiveDataTransformer(this, FloatApplicative::convert)
}

fun <T> LiveData<T>.float(defaultValue: Float): LiveData<Float> {
    return LiveDataTransformer(this) {
        if (it == null) {
            return@LiveDataTransformer defaultValue
        }
        FloatApplicative.convert(it)
    }
}

fun <T> LiveData<T>.doubleOrNull(): LiveData<Double?> {
    return LiveDataTransformer(this, DoubleApplicative::convertOrNull)
}

fun <T> LiveData<T>.double(): LiveData<Double> {
    return LiveDataTransformer(this, DoubleApplicative::convert)
}

fun <T> LiveData<T>.double(defaultValue: Double): LiveData<Double> {
    return LiveDataTransformer(this) {
        if (it == null) {
            return@LiveDataTransformer defaultValue
        }
        DoubleApplicative.convert(it)
    }
}

fun <T> LiveData<T>.stringOrNull(): LiveData<String?> {
    return LiveDataTransformer(this, StringApplicative::convertOrNull)
}

fun <T> LiveData<T>.string(): LiveData<String> {
    return LiveDataTransformer(this, StringApplicative::convert)
}

fun <T> LiveData<T>.string(defaultValue: String): LiveData<String> {
    return LiveDataTransformer(this) {
        if (it == null) {
            return@LiveDataTransformer defaultValue
        }
        StringApplicative.convert(it)
    }
}

fun <T> LiveData<T>.listOrNull(): LiveData<List<*>?> {
    return LiveDataTransformer(this, ListApplicative::convertOrNull)
}

fun <T> LiveData<T>.list(): LiveData<List<*>> {
    return list(emptyList<Any?>())
}

fun <T> LiveData<T>.list(defaultValue: List<*>): LiveData<List<*>> {
    return LiveDataTransformer(this) {
        if (it == null) {
            return@LiveDataTransformer defaultValue
        }
        ListApplicative.convert(it)
    }
}

fun <T> LiveData<T>.mapOrNull(): LiveData<Map<*, *>?> {
    return LiveDataTransformer(this, MapApplicative::convertOrNull)
}

fun <T> LiveData<T>.map(): LiveData<Map<*, *>> {
    return map(emptyMap<Any?, Any?>())
}

fun <T> LiveData<T>.map(defaultValue: Map<*, *>): LiveData<Map<*, *>> {
    return LiveDataTransformer(this) {
        if (it == null) {
            return@LiveDataTransformer defaultValue
        }
        MapApplicative.convert(it)
    }
}

@JvmName("mapToNonNullList")
fun <T> LiveData<List<*>>.mapTo(transformer: Function<Any?, T>): LiveData<List<T>> {
    return LiveDataTransformer(this) { it.map(transformer::apply) }
}

@JvmName("mapToNullableList")
fun <T> LiveData<List<*>?>.mapTo(transformer: Function<Any?, T>): LiveData<List<T>?> {
    return LiveDataTransformer(this) { it?.map(transformer::apply) }
}

@JvmName("mapToNonNullMap")
fun <K, V> LiveData<Map<*, *>>.mapTo(transformer: Function<Map.Entry<Any?, Any?>, Pair<K, V>>): LiveData<Map<K, V>> {
    return LiveDataTransformer(this) { it.entries.associate(transformer::apply) }
}

@JvmName("mapToNullableMap")
fun <K, V> LiveData<Map<*, *>?>.mapTo(transformer: Function<Map.Entry<Any?, Any?>, Pair<K, V>>): LiveData<Map<K, V>?> {
    return LiveDataTransformer(this) { it?.entries?.associate(transformer::apply) }
}

fun LiveData<Map<*, *>>.normalize(): LiveData<Map<String, Any?>> {
    return LiveDataTransformer(this) { it.mapKeys(StringApplicative::convert) }
}

fun <T> LiveData<T>.intListOrNull(): LiveData<List<Int>?> {
    return listOrNull().mapTo(IntApplicative::convert)
}

fun <T> LiveData<T>.intList(): LiveData<List<Int>> {
    return list().mapTo(IntApplicative::convert)
}

fun <T> LiveData<T>.longListOrNull(): LiveData<List<Long>?> {
    return listOrNull().mapTo(LongApplicative::convert)
}

fun <T> LiveData<T>.longList(): LiveData<List<Long>> {
    return list().mapTo(LongApplicative::convert)
}

fun <T> LiveData<T>.floatListOrNull(): LiveData<List<Float>?> {
    return listOrNull().mapTo(FloatApplicative::convert)
}

fun <T> LiveData<T>.floatList(): LiveData<List<Float>> {
    return list().mapTo(FloatApplicative::convert)
}

fun <T> LiveData<T>.doubleListOrNull(): LiveData<List<Double>?> {
    return listOrNull().mapTo(DoubleApplicative::convert)
}

fun <T> LiveData<T>.doubleList(): LiveData<List<Double>> {
    return list().mapTo(DoubleApplicative::convert)
}

fun <T> LiveData<T>.stringListOrNull(): LiveData<List<String>?> {
    return listOrNull().mapTo(StringApplicative::convert)
}

fun <T> LiveData<T>.stringList(): LiveData<List<String>> {
    return list().mapTo(StringApplicative::convert)
}

fun <T> LiveData<T>.stringList(defaultValue: List<String>): LiveData<List<String>> {
    return LiveDataTransformer(this) {
        if (it == null) {
            return@LiveDataTransformer defaultValue
        }
        ListApplicative.convert(it)?.map(StringApplicative::convert) ?: defaultValue
    }
}

fun <T> LiveData<T>.mapList(): LiveData<List<Map<*, *>>> {
    return list(emptyList<Map<*, *>>()).mapTo(MapApplicative::convert)
}

fun <T> LiveData<T>.normalizeMap(): LiveData<Map<String, Any?>> {
    return map().normalize()
}