package top.lanscarlos.vulpecula.utils

import taboolib.common5.Coerce

/**
 * Vulpecular
 * top.lanscarlos.vulpecular.utils
 *
 * @author Lanscarlos
 * @since 2022-02-27 16:45
 */

fun Any?.asBoolean(): Boolean? {
    return Coerce.asBoolean(this).let { if (it.isPresent) it.get() else null }
}

fun Any?.asBoolean(def: Boolean): Boolean {
    return Coerce.asBoolean(this).let { if (it.isPresent) it.get() else def }
}

fun Any?.toBoolean(def: Boolean? = null): Boolean {
    if (this == null && def != null) return def
    return Coerce.toBoolean(this)
}

fun Any?.asShort(): Short? {
    return Coerce.asShort(this).let { if (it.isPresent) it.get() else null }
}

fun Any?.asShort(def: Short): Short {
    return Coerce.asShort(this).let { if (it.isPresent) it.get() else def }
}

fun Any?.toShort(def: Short? = null): Short {
    if (this == null && def != null) return def
    return Coerce.toShort(this)
}

fun Any?.asInt(): Int? {
    return Coerce.asInteger(this).let { if (it.isPresent) it.get() else null }
}

fun Any?.asInt(def: Int): Int {
    return Coerce.asInteger(this).let { if (it.isPresent) it.get() else def }
}

fun Any?.toInt(def: Int? = null): Int {
    if (this == null && def != null) return def
    return Coerce.toInteger(this)
}

fun Any?.asLong(): Long? {
    return Coerce.asLong(this).let { if (it.isPresent) it.get() else null }
}

fun Any?.asLong(def: Long): Long {
    return Coerce.asLong(this).let { if (it.isPresent) it.get() else def }
}

fun Any?.toLong(def: Long? = null): Long {
    if (this == null && def != null) return def
    return Coerce.toLong(this)
}

fun Any?.asFloat(): Float? {
    return Coerce.asFloat(this).let { if (it.isPresent) it.get() else null }
}

fun Any?.asFloat(def: Float): Float {
    return Coerce.asFloat(this).let { if (it.isPresent) it.get() else def }
}

fun Any?.toFloat(def: Float? = null): Float {
    if (this == null && def != null) return def
    return Coerce.toFloat(this)
}

fun Any?.asDouble(): Double? {
    return Coerce.asDouble(this).let { if (it.isPresent) it.get() else null }
}

fun Any?.asDouble(def: Double): Double {
    return Coerce.asDouble(this).let { if (it.isPresent) it.get() else def }
}

fun Any?.toDouble(def: Double? = null): Double {
    if (this == null && def != null) return def
    return Coerce.toDouble(this)
}

fun Any?.asString(): String? {
    return Coerce.asString(this).let { if (it.isPresent) it.get() else null }
}

fun Any?.asString(def: String): String {
    return Coerce.asString(this).let { if (it.isPresent) it.get() else def }
}

fun Any?.asList(): List<Any?>? {
    return Coerce.asList(this).let { if (it.isPresent) it.get() else null }
}

fun Any?.asList(def: List<Any?>): List<Any?> {
    return Coerce.asList(this).let { if (it.isPresent) it.get() else def }
}

//fun Any?.toList(def: List<Any?>? = null): List<Any?> {
//    if (this == null && def != null) return def
//    return Coerce.toList(this)
//}