package top.lanscarlos.vulpecula.module.bacikal.annotation

import kotlin.reflect.KClass

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.annotation
 *
 * @author Lanscarlos
 * @since 2025/8/3
 */
annotation class Property(val id: String = "", val bind: KClass<*>)
