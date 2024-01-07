package top.lanscarlos.vulpecula.bacikal.action.canvas

import taboolib.module.kether.player
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.bacikalSwitch

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas
 *
 * @author Lanscarlos
 * @since 2024-01-07 10:29
 */
object ActionParticles {

    /**
     *
     * particles play at &loc -x -xx
     *
     * */
    @BacikalParser("particles")
    fun parser() = bacikalSwitch {
        case("play") {
            combine(
                optional("at", then = location()),
                argument("viewer", then = ActionViewers.viewers()),
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
            ) { location, viewers, type, count, speed, offset, spread, velocity, size, color, transition, material, data, name, lore, model ->
                val brush = CanvasBrush()
                ActionBrush.modify(
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

                brush.draw(location ?: this.player().location, viewers ?: listOf(this.player()))
            }
        }
    }
}