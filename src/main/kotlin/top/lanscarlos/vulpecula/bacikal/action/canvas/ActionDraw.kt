package top.lanscarlos.vulpecula.bacikal.action.canvas

import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveLocation
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.readerOf
import top.lanscarlos.vulpecula.bacikal.action.canvas.pattern.CanvasPattern
import top.lanscarlos.vulpecula.bacikal.bacikal
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-09 00:20
 */
object ActionDraw {

    @BacikalParser(
        id = "draw",
        name = ["draw"],
        namespace = "vulpecula-canvas"
    )
    fun parser() = bacikal {
        combine(
            trim("at", then = location()),
            optional("using", "with", "by", then = readerOf { r ->
                r.expectToken("pattern")
                r.nextToken()
            }),
            argument("index", "i", then = int(0), def = 0)
        ) { location, template, index ->

            if (template == null) {
                draw(this, location)
                return@combine true
            }

            // 使用图案绘制
            val patterns = this.getVariable<List<CanvasPattern>>(ActionCanvas.VARIABLE_PATTERNS) ?: return@combine setOf<Location>()

            if (index < 0 || index > patterns.size) {
                error("Index out of bounds of patterns.")
            }

            when (template) {
                "point" -> {
                    if (index == 0) {
                        draw(this, patterns.map { it.point(location) })
                    } else {
                        draw(this, patterns[index - 1].point(location))
                    }
                }
                "shape" -> {
                    if (index == 0) {
                        draw(this, patterns.flatMap { it.shape(location) })
                    } else {
                        draw(this, patterns[index - 1].shape(location))
                    }
                }
                else -> error("Unknown pattern sub action.")
            }
            return@combine true
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