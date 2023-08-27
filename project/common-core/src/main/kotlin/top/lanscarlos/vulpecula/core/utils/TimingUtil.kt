package top.lanscarlos.vulpecula.core.utils

import taboolib.common5.Coerce

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.utils
 *
 * @author Lanscarlos
 * @since 2023-08-27 12:20
 */

fun timing(): Long {
    return System.nanoTime()
}

fun timing(start: Long): Double {
    return Coerce.format((System.nanoTime() - start).div(1000000.0))
}