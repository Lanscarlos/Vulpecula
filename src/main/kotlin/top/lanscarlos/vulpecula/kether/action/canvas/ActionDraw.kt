package top.lanscarlos.vulpecula.kether.action.canvas

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.action.ActionLocation
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.LocationLiveData
import top.lanscarlos.vulpecula.kether.live.VectorLiveData
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-09 00:20
 */
class ActionDraw(val raw: Any) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {

        val base = frame.getVariable<Location>(ActionCanvas.VARIABLE_ORIGIN) ?: frame.unsafePlayer()?.location ?: ActionLocation.def

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

        val loc = when (raw) {
            is LocationLiveData -> {
                raw.get(frame, base)
            }
            is VectorLiveData -> {
                val offset = raw.get(frame, Vector(0, 0, 0))
                base.clone().add(offset)
            }
            is ParsedAction<*> -> {
                when (val it = frame.run(raw).join()) {
                    is Location -> it
                    is org.bukkit.Location -> it.toProxyLocation()
                    is ProxyPlayer -> it.location
                    is Entity -> it.location.toProxyLocation()
                    is Vector -> Location(base.world, it.x, it.y, it.z)
                    is org.bukkit.util.Vector -> Location(base.world, it.x, it.y, it.z)
                    is Collection<*> -> {
                        val locations = it.mapNotNull { content ->
                            when (content) {
                                is Location -> content
                                is org.bukkit.Location -> content.toProxyLocation()
                                is ProxyPlayer -> content.location
                                is Entity -> content.location.toProxyLocation()
                                is Vector -> Location(base.world, content.x, content.y, content.z)
                                is org.bukkit.util.Vector -> Location(base.world, content.x, content.y, content.z)
                                else -> null
                            }
                        }

                        // 绘制除最后一个坐标外所有坐标
                        if (locations.size >= 2) {
                            for (i in 0 until locations.lastIndex) {
                                brush.draw(locations[i], viewers)
                            }
                        }

                        // 返回最后一个坐标
                        locations.lastOrNull() ?: base
                    }
                    else -> base
                }
            }
            else -> base
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
                    ActionDraw(reader.nextBlock())
                }
                else -> {
                    reader.reset()
                    ActionDraw(reader.readVector(true))
                }
            }
        }
    }
}