package top.lanscarlos.vulpecula.common.config

import top.lanscarlos.vulpecula.common.applicative.BooleanApplicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2024-12-16 01:35
 */
interface DynamicConfig {

    fun contains(key: String): Boolean

    fun get(key: String): Any?

    fun set(key: String, value: Any?)

    fun remove(key: String)

    fun getBoolean(key: String): Boolean? {
        return get(key)?.let(BooleanApplicative::apply)
    }

    fun getBoolean(key: String, def: Boolean): Boolean {
        return get(key)?.let { BooleanApplicative.apply(it, def) } ?: def
    }

    fun readBoolean(key: String): DynamicSection<Boolean> {}

}