package top.lanscarlos.vulpecula.bacikal.action.canvas

import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveLocation
import top.lanscarlos.vulpecula.bacikal.bacikalSwitch
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-09 00:20
 */
object ActionDraw {

    /**
     *
     * 定义基准点, 即原点
     * draw origin &loc
     * draw base &loc
     *
     * 根据偏移量作画
     * draw &x &y &z
     *
     * 根据向量偏移作画
     * draw by/with &vec
     *
     * 根据坐标直接作画
     * draw at &loc
     * draw at pattern next
     * draw at pattern points
     *
     * */
    @BacikalParser(
        id = "draw",
        name = ["draw"],
        namespace = "vulpecula-canvas"
    )
    fun parser() = bacikalSwitch {
        case("origin", "base") {
            combine(
                locationOrNull()
            ) { location ->
                this.setVariable(ActionCanvas.VARIABLE_ORIGIN, location)
            }
        }
        case("by", "with") {
            combine(
                vector()
            ) { vector ->
                draw(this, vector)
            }
        }
        case("at") {
            combine(
                location()
            ) { location ->
                draw(this, location)
            }
        }
        other {
            combine(
                double(0.0),
                double(0.0),
                double(0.0)
            ) { x, y, z ->
                draw(this, Vector(x, y, z))
            }
        }
    }

    fun draw(frame: ScriptFrame, target: Any) {

        // 获取观察者对象
        val viewers = frame.getVariable<Collection<ProxyPlayer>>(ActionCanvas.VARIABLE_VIEWERS) ?: setOf(frame.player())
        if (viewers.isEmpty()) {
            // 观察者为空, 不作画
            return
        }

        // 获取笔刷对象
        val brush = frame.getVariable<CanvasBrush>(ActionCanvas.VARIABLE_BRUSH) ?: CanvasBrush().also {
            frame.setVariable(ActionCanvas.VARIABLE_BRUSH, it)
        }

        // 获取原点
        val base = frame.getVariable<Location>(ActionCanvas.VARIABLE_ORIGIN) ?: frame.playerOrNull()?.location

        // 获取作画位置
        val locations = when (target) {
            is Location -> listOf(target)
            is Vector -> listOf(base?.clone()?.add(target) ?: error("No base or origin selected."))
            is Array<*> -> {
                target.mapNotNull { it?.liveLocation }
            }
            is Collection<*> -> {
                target.mapNotNull { it?.liveLocation }
            }
            else -> listOf(base ?: error("No base or origin selected."))
        }

        brush.draw(locations, viewers)
    }
}