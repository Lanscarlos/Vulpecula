package top.lanscarlos.vulpecula.config

import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2022-12-15 18:22
 */
interface DynamicConfigNode<R> {
    operator fun getValue(any: Any?, property: KProperty<*>): R
}