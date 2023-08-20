package top.lanscarlos.vulpecula.legacy.bacikal

import kotlin.reflect.KClass

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-03-19 22:12
 */
annotation class BacikalProperty(
    val id: String,
    val bind: KClass<*>,
    val shared: Boolean = true
)
