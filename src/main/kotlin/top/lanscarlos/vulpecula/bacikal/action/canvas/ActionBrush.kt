package top.lanscarlos.vulpecula.bacikal.action.canvas

import taboolib.common.platform.ProxyParticle
import taboolib.common.util.Vector
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.bacikal
import top.lanscarlos.vulpecula.utils.*
import java.awt.Color

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-08 23:25
 */

object ActionBrush {

    @BacikalParser(
        id = "brush",
        name = ["brush", "pen"],
        namespace = "vulpecula-canvas"
    )
    fun parser() = bacikal {
        combine(
            argument("type", "t", then = text(display = "type")),
            argument("count", "c", then = int(display = "count")),
            argument("speed", "sp", then = double(display = "speed")),
            argument("offset", "o", then = vector(display = "offset")),
            argument("spread", "s", then = vector(display = "spread")),
            argument("velocity", "vel", "v", then = vector(display = "velocity")),
            argument("size", then = float(display = "size")),
            argument("color", then = color(display = "color")),
            argument("transition", then = color(display = "transition")),
            argument("material", "mat", then = text(display = "material")),
            argument("data", then = int(display = "data")),
            argument("name", then = text(display = "name")),
            argument("lore", then = multiline(display = "lore")),
            argument("customModelData", "model", then = int(display = "model"))
        ) { type, count, speed, offset, spread, velocity, size, color, transition, material, data, name, lore, model ->

            // 获取笔刷对象
            val brush = this.getVariable<CanvasBrush>(ActionCanvas.VARIABLE_BRUSH) ?: CanvasBrush().also {
                this.setVariable(ActionCanvas.VARIABLE_BRUSH, it)
            }

            modify(
                brush,
                type,
                count,
                speed,
                offset,
                spread,
                velocity,
                size,
                color,
                transition,
                material,
                data,
                name,
                lore,
                model
            )
        }
    }

    fun modify(
        brush: CanvasBrush,
        type: String?,
        count: Int?,
        speed: Double?,
        offset: Vector?,
        spread: Vector?,
        velocity: Vector?,
        size: Float?,
        color: Color?,
        transition: Color?,
        material: String?,
        data: Int?,
        name: String?,
        lore: List<String>?,
        model: Int?
    ) {
        if (type != null) {
            brush.particle = ProxyParticle.values().firstOrNull {
                it.name.equals(type, true)
            } ?: error("Unknown particle type: \"$type\"!")
        }

        if (count != null) {
            brush.count = count
        }

        if (speed != null) {
            brush.speed = speed
        }

        if (offset != null) {
            brush.offset = offset
        }

        if (spread != null) {
            brush.vector = spread
        }

        if (velocity != null) {
            brush.vector = velocity
            brush.count = 0
            if (brush.speed == 0.0) brush.speed = 0.15
        }

        if (size != null) {
            brush.size = size
        }

        if (color != null) {
            brush.color = color
        }

        if (transition != null) {
            brush.transition = transition
        }

        if (material != null) {
            brush.material = material
        }

        if (data != null) {
            brush.data = data
        }

        if (name != null) {
            brush.name = name
        }

        if (lore != null) {
            brush.lore = lore
        }

        if (model != null) {
            brush.model = model
        }
    }
}