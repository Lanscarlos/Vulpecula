package top.lanscarlos.vulpecula.kether

import kotlin.reflect.KClass

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether
 *
 * @author Lanscarlos
 * @since 2022-10-18 20:14
 */
annotation class VulKetherProperty(
    val id: String,
    val bind: KClass<*>,
    val shared: Boolean = true,
    val generic: Boolean = false
)
