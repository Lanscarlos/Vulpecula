package top.lanscarlos.vulpecula.bacikal.action.canvas

import taboolib.common.util.Location
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.bacikal.action.canvas.pattern.CanvasPattern
import top.lanscarlos.vulpecula.bacikal.bacikalSwitch
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-10 11:14
 */
object ActionPattern {

    /**
     *
     * 获取图案的下一点坐标
     * pattern next [origin &origin] [by &pattern]
     *
     * 获取图案的所有点坐标
     * pattern points [origin &origin] [by &pattern]
     *
     * 定义图案
     * pattern [other token]
     * pattern line [from xxx] to xxx
     * pattern
     *
     * */
    @BacikalParser(
        id = "pattern",
        name = ["pattern"],
        namespace = "vulpecula-canvas"
    )
    fun parser() = bacikalSwitch {
        case("next") {
            combine(
                optional("origin", then = location()),
                optional("by", then = pattern())
            ) { origin, _pattern ->
                val pattern = _pattern ?: this.getVariable<CanvasPattern>(ActionCanvas.VARIABLE_PATTERN)
                ?: error("No canvas pattern selected.")

                val location =
                    origin ?: this.getVariable<Location>(ActionCanvas.VARIABLE_ORIGIN) ?: this.playerOrNull()?.location
                    ?: error("No canvas base or origin location selected.")

                pattern.nextPoint(location)
            }
        }
        case("points") {
            combine(
                optional("origin", then = location()),
                optional("by", then = pattern())
            ) { origin, _pattern ->
                val pattern = _pattern ?: this.getVariable<CanvasPattern>(ActionCanvas.VARIABLE_PATTERN)
                ?: error("No canvas pattern selected.")

                val location =
                    origin ?: this.getVariable<Location>(ActionCanvas.VARIABLE_ORIGIN) ?: this.playerOrNull()?.location
                    ?: error("No canvas base or origin location selected.")

                pattern.points(location)
            }
        }
        other {
            val name = this.nextToken()
            CanvasPattern.getResolver(name)?.resolve(this) ?: error("Unknown pattern type: \"$name\"")
        }
    }

    fun pattern(): LiveData<CanvasPattern?> {
        return LiveData.frameBy { it as? CanvasPattern }
    }
}