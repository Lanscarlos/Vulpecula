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

fun <T> LiveData<T>.boolean(): LiveData<Boolean> {
    return ProxyLiveData(this, BooleanApplicative::convertOrThrow)
}

fun <T> LiveData<T>.boolean(defaultValue: Boolean): LiveData<Boolean> {
    return ProxyLiveData(this) {
        BooleanApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.int(): LiveData<Int> {
    return ProxyLiveData(this, IntApplicative::convertOrThrow)
}

fun <T> LiveData<T>.int(defaultValue: Int): LiveData<Int> {
    return ProxyLiveData(this) {
        IntApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.long(): LiveData<Long> {
    return ProxyLiveData(this, LongApplicative::convertOrThrow)
}

fun <T> LiveData<T>.long(defaultValue: Long): LiveData<Long> {
    return ProxyLiveData(this) {
        LongApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.float(): LiveData<Float> {
    return ProxyLiveData(this, FloatApplicative::convertOrThrow)
}

fun <T> LiveData<T>.float(defaultValue: Float): LiveData<Float> {
    return ProxyLiveData(this) {
        FloatApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.double(): LiveData<Double> {
    return ProxyLiveData(this, DoubleApplicative::convertOrThrow)
}

fun <T> LiveData<T>.double(defaultValue: Double): LiveData<Double> {
    return ProxyLiveData(this) {
        DoubleApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.string(): LiveData<String> {
    return ProxyLiveData(this, StringApplicative::convertOrThrow)
}

fun <T> LiveData<T>.string(defaultValue: String): LiveData<String> {
    return ProxyLiveData(this) {
        StringApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.list(): LiveData<List<*>> {
    return ProxyLiveData(this, ListApplicative::convertOrThrow)
}

fun <T> LiveData<T>.list(defaultValue: List<*>): LiveData<List<*>> {
    return ProxyLiveData(this) {
        ListApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T> LiveData<T>.map(): LiveData<Map<*, *>> {
    return ProxyLiveData(this, MapApplicative::convertOrThrow)
}

fun <T> LiveData<T>.map(defaultValue: Map<*, *>): LiveData<Map<*, *>> {
    return ProxyLiveData(this) {
        MapApplicative.convertOrNull(it) ?: defaultValue
    }
}

fun <T, R> LiveData<T>.convert(transformer: Function<T, R>): LiveData<R> {
    return ProxyLiveData(this, transformer)
}

fun <T> LiveData<List<*>>.mapTo(transformer: Function<Any?, T>): LiveData<List<T>> {
    return ProxyLiveData(this) {
        it.map(transformer::apply)
    }
}

fun <K, V> LiveData<Map<*, *>>.mapTo(transformer: Function<Map.Entry<Any?, Any?>, Pair<K, V>>): LiveData<Map<K, V>> {
    return ProxyLiveData(this) {
        it.entries.associate(transformer::apply)
    }
}

fun LiveData<Map<*, *>>.normalize(): LiveData<Map<String, Any?>> {
    return ProxyLiveData(this) {
        it.mapKeys(StringApplicative::convertOrThrow)
    }
}

fun <T> LiveData<T>.intList(): LiveData<List<Int>> {
    return list().mapTo(IntApplicative::convertOrThrow)
}

fun <T> LiveData<T>.longList(): LiveData<List<Long>> {
    return list().mapTo(LongApplicative::convertOrThrow)
}

fun <T> LiveData<T>.floatList(): LiveData<List<Float>> {
    return list().mapTo(FloatApplicative::convertOrThrow)
}

fun <T> LiveData<T>.doubleList(): LiveData<List<Double>> {
    return list().mapTo(DoubleApplicative::convertOrThrow)
}

fun <T> LiveData<T>.stringList(): LiveData<List<String>> {
    return list().mapTo(StringApplicative::convertOrThrow)
}

fun <T> LiveData<T>.normalizeMap(): LiveData<Map<String, Any?>> {
    return map().normalize()
}