package top.lanscarlos.vulpecula.legacy.utils

import taboolib.common5.Coerce

fun timing(): Long {
    return System.nanoTime()
}

fun timing(start: Long): Double {
    return Coerce.format((System.nanoTime() - start).div(1000000.0))
}