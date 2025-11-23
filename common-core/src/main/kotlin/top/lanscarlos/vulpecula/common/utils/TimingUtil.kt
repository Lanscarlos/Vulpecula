package top.lanscarlos.vulpecula.common.utils

import taboolib.common5.Coerce

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2024-05-15 10:06
 */

/**
 * 开始计时
 * */
fun timing(): Long {
    return System.nanoTime()
}

/**
 * 结束计时
 *
 * @return 毫秒数
 * */
fun timing(start: Long): Double {
    return Coerce.format((System.nanoTime() - start).div(1000000.0))
}