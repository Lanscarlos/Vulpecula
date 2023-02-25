package top.lanscarlos.vulpecula.utils.config

import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils.config
 *
 * @author Lanscarlos
 * @since 2022-12-15 18:22
 */
interface VulConfigNode<R> {
    operator fun getValue(any: Any?, property: KProperty<*>): R
}