package top.lanscarlos.vulpecula.kether.action.effect

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.action.ActionLocation
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.LocationLiveData
import top.lanscarlos.vulpecula.kether.live.VectorLiveData
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.effect
 *
 * @author Lanscarlos
 * @since 2022-11-09 00:20
 */
class ActionDraw(val raw: LiveData<*>) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {

        val base = frame.getVariable<Location>(ActionCanvas.VARIABLE_ORIGIN) ?: frame.unsafePlayer()?.location ?: ActionLocation.def

        val loc = when (raw) {
            is LocationLiveData -> {
                raw.get(frame, base)
            }
            is VectorLiveData -> {
                val offset = raw.get(frame, Vector(0, 0, 0))
                base.clone().add(offset)
            }
            else -> base
        }

        val brush = frame.getVariable<CanvasBrush>(ActionCanvas.VARIABLE_BRUSH) ?: CanvasBrush().also {
            frame.setVariable(ActionCanvas.VARIABLE_BRUSH, it)
        }

        val viewers = when (val value = frame.getVariable<Any>(ActionCanvas.VARIABLE_VIEWERS)) {
            is ProxyPlayer -> listOf(value)
            is Player -> listOf(adaptPlayer(value))
            is Collection<*> -> {
                value.mapNotNull {
                    when (it) {
                        is ProxyPlayer -> it
                        is Player -> adaptPlayer(it)
                        else -> null
                    }
                }
            }
            else -> listOf(frame.player())
        }

        brush.draw(loc, viewers)

        return CompletableFuture.completedFuture(null)
    }

    companion object {

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
        @VulKetherParser(
            id = "draw",
            name = ["draw", "draw"],
            namespace = "vulpecula-canvas"
        )
        fun parser() = scriptParser { reader ->
            reader.mark()
            when (reader.nextToken()) {
                "origin", "base" -> {
                    val raw = reader.readLocation()
                    actionNow {
                        this.setVariable(ActionCanvas.VARIABLE_ORIGIN, raw.get(this, this.unsafePlayer()?.location ?: ActionLocation.def))
                    }
                }
                "by", "with" -> {
                    ActionDraw(VectorLiveData(reader.nextBlock()))
                }
                "at" -> {
                    ActionDraw(reader.readLocation())
                }
                else -> {
                    reader.reset()
                    ActionDraw(reader.readVector())
                }
            }
        }
    }
}