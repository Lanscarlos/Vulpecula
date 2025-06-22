package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.pattern

import taboolib.common.util.Location

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2023-06-29 11:29
 */
interface Transformer {

    /**
     * 变换
     * @param origin 原点
     * @param target 目标点
     * */
    fun transform(origin: Location, target: Location): Location

    /**
     * 变换
     * */
    fun transform(origin: Location, target: Collection<Location>): Collection<Location>
}