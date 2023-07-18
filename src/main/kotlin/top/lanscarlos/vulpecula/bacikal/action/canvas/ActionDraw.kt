package top.lanscarlos.vulpecula.bacikal.action.canvas

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.module.kether.*
import taboolib.platform.type.BukkitPlayer
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.vulpecula.bacikal.BacikalParser
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
            optional("at", then = location()),
            optional("using", "with", "by", then = readerOf { r ->
                r.expectToken("pattern")
                r.nextToken()
            }),
            argument("index", "i", then = int(), def = 0)
        ) { location, template, index ->

            // 获取观察者对象
            val viewers = this.getVariable<Collection<ProxyPlayer>>(ActionCanvas.VARIABLE_VIEWERS) ?: setOf(this.player())
            if (viewers.isEmpty()) {
                // 观察者为空, 不作画
                return@combine true
            }

            // 获取笔刷对象
            val brush = this.getVariable<CanvasBrush>(ActionCanvas.VARIABLE_BRUSH) ?: CanvasBrush().also {
                this.setVariable(ActionCanvas.VARIABLE_BRUSH, it)
            }

            // 获取绘制坐标或图案原点
            val target = location ?: when (val bind = this.getVariable<Any?>(ActionCanvas.VARIABLE_TARGET)) {
                is Block -> bind.location.toProxyLocation()
                is Entity -> bind.location.toProxyLocation()
                is BukkitPlayer -> bind.location
                else -> playerOrNull()?.location ?: error("No target location selected.")
            }

            // 无图案绘制
            if (template == null) {
                brush.draw(target, viewers)
                return@combine true
            }

            // 使用图案绘制
            val patterns = this.getVariable<List<CanvasPattern>>(ActionCanvas.VARIABLE_PATTERNS) ?: return@combine setOf<Location>()

            if (index < -1 || index >= patterns.size) {
                error("Pattern index \"$index\" is out of bounds of patterns.")
            }

            when (template) {
                "point" -> {
                    if (index == -1) {
                        brush.draw(patterns.map { it.point(target) }, viewers)
                    } else {
                        brush.draw(patterns[index].point(target), viewers)
                    }
                }
                "shape" -> {
                    if (index == -1) {
                        brush.draw(patterns.flatMap { it.shape(target) }, viewers)
                    } else {
                        brush.draw(patterns[index].shape(target), viewers)
                    }
                }
                else -> error("Unknown pattern sub action：\"$template\"")
            }
            return@combine true
        }
    }
}