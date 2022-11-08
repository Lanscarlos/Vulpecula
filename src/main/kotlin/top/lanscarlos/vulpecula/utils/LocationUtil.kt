package top.lanscarlos.vulpecula.utils

import taboolib.common.util.Location
import taboolib.common.util.Vector
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2022-11-06 21:48
 */

/**
 * 根据坐标的俯仰角得到单位向量
 * */
fun Location.toVectorFix(): Vector {
    val pitch = Math.toRadians(-pitch.toDouble())
    return Vector(
        sin(Math.toRadians(-yaw.toDouble())) * cos(pitch),
        sin(pitch),
        cos(Math.toRadians(yaw.toDouble())) * cos(pitch)
    )
}