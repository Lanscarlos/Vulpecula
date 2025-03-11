package top.lanscarlos.vulpecula.common.livedata

import top.lanscarlos.vulpecula.common.applicative.*
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.livedata
 *
 * @author Lanscarlos
 * @since 2025-03-10 19:04
 */

fun <T> LiveData<T>.convertToBoolean(): LiveData<Boolean> {
    return ProxyLiveData(this, BooleanApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToBoolean(defaultValue: Boolean): LiveData<Boolean> {
    return ProxyLiveData(this) {
        BooleanApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.convertToInt(): LiveData<Int> {
    return ProxyLiveData(this, IntApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToInt(defaultValue: Int): LiveData<Int> {
    return ProxyLiveData(this) {
        IntApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.convertToLong(): LiveData<Long> {
    return ProxyLiveData(this, LongApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToLong(defaultValue: Long): LiveData<Long> {
    return ProxyLiveData(this) {
        LongApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.convertToFloat(): LiveData<Float> {
    return ProxyLiveData(this, FloatApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToFloat(defaultValue: Float): LiveData<Float> {
    return ProxyLiveData(this) {
        FloatApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.convertToDouble(): LiveData<Double> {
    return ProxyLiveData(this, DoubleApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToDouble(defaultValue: Double): LiveData<Double> {
    return ProxyLiveData(this) {
        DoubleApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.convertToString(): LiveData<String> {
    return ProxyLiveData(this, StringApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToString(defaultValue: String): LiveData<String> {
    return ProxyLiveData(this) {
        StringApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.convertToList(): LiveData<List<*>> {
    return ProxyLiveData(this, ListApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToList(defaultValue: List<*>): LiveData<List<*>> {
    return ProxyLiveData(this) {
        ListApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.convertToMap(): LiveData<Map<*, *>> {
    return ProxyLiveData(this, MapApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToMap(defaultValue: Map<*, *>): LiveData<Map<*, *>> {
    return ProxyLiveData(this) {
        MapApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T, R> LiveData<T>.convert(transformer: Function<T, R>): LiveData<R> {
    return ProxyLiveData(this, transformer)
}

fun <T> LiveData<List<*>>.map(transformer: Function<Any?, T>): LiveData<List<T>> {
    return ProxyLiveData(this) {
        it.map(transformer::apply)
    }
}

fun LiveData<Map<*, *>>.normalize(): LiveData<Map<String, Any?>> {
    return ProxyLiveData(this) {
        it.mapKeys(StringApplicative::convertOrThrow)
    }
}

fun <K, V> LiveData<Map<*, *>>.map(transformer: Function<Map.Entry<Any?, Any?>, Pair<K, V>>): LiveData<Map<K, V>> {
    return ProxyLiveData(this) {
        it.entries.associate(transformer::apply)
    }
}

fun <T> LiveData<T>.convertToIntList(): LiveData<List<Int>> {
    return convertToList().map(IntApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToLongList(): LiveData<List<Long>> {
    return convertToList().map(LongApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToFloatList(): LiveData<List<Float>> {
    return convertToList().map(FloatApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToDoubleList(): LiveData<List<Double>> {
    return convertToList().map(DoubleApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToStringList(): LiveData<List<String>> {
    return convertToList().map(StringApplicative::convertOrThrow)
}

fun <T> LiveData<T>.convertToNormalizeMap(): LiveData<Map<String, Any?>> {
    return convertToMap().normalize()
}