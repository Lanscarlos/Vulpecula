package top.lanscarlos.vulpecula.bacikal.property

import kotlin.reflect.KClass

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property
 *
 * @author Lanscarlos
 * @since 2023-08-25 00:59
 */
annotation class BacikalProperty(val id: String, val bind: KClass<*>)
