package top.lanscarlos.vulpecula.bacikal.action.canvas.pattern

import taboolib.common.util.Location

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2023-06-29 15:40
 */
class PatternTransformation(val pattern: CanvasPattern) : CanvasPattern {

    val transformers = mutableListOf<Transformer>()

    override fun point(origin: Location): Location {
        // 防止原点被修改
        var location = pattern.point(origin.clone())

        // 复制原点副本，让变换器能够对原点副本修改
        val copyOrigin = origin.clone()
        for (transformer in transformers) {
            location = transformer.transform(copyOrigin, location)
        }
        return location
    }

    override fun shape(origin: Location): Collection<Location> {
        // 防止原点被修改
        var locations = pattern.shape(origin.clone())

        // 复制原点副本，让变换器能够对原点副本修改
        val copyOrigin = origin.clone()
        for (transformer in transformers) {
            locations = transformer.transform(copyOrigin, locations)
        }
        return locations
    }
}