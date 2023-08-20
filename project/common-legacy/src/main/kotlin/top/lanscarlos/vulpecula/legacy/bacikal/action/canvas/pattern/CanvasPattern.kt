package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.pattern

import taboolib.common.util.Location

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2022-11-10 10:32
 */
interface CanvasPattern {

    /**
     * 获取图案的下一个点坐标
     *
     * @param origin 原点，当内置原点时，优先使用内置原点
     * @return 点坐标
     * */
    fun point(origin: Location): Location

    /**
     * 获取图案的所有点坐标
     *
     * @param origin 原点，当内置原点时，优先使用内置原点
     * @return 坐标集合
     * */
    fun shape(origin: Location): Collection<Location>

}