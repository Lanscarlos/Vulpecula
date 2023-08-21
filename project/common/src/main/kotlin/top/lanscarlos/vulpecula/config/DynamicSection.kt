package top.lanscarlos.vulpecula.config

import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2023-08-21 21:34
 */
interface DynamicSection<T> {

    fun getValue(): T

    operator fun getValue(any: Any?, property: KProperty<*>): T

}