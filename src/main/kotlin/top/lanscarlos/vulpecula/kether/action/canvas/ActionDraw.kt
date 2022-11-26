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

        val base = frame.getVariable<Location>(ActionCanvas.VARIABLE_ORIGIN) ?: frame.unsafePlayer()?.location

        when (raw) {
            is LocationLiveData -> {
                return if (base != null) {
                    raw.get(frame, base).thenApply {
                        brush.draw(it, viewers)
                    }
                } else {
                    raw.getOrNull(frame).thenApply {
                        brush.draw(it ?: error("No location selected."), viewers)
                    }
                }
            }
            is VectorLiveData -> {
                return raw.get(frame, Vector(0, 0, 0)).thenApply { offset ->
                    val location = base?.clone()?.add(offset) ?: error("No base or origin selected.")
                    brush.draw(location, viewers)
                }
            }
            is ParsedAction<*> -> {
                return frame.run(raw).thenApply { value ->
                    val location = when (value) {
                        is Location -> value
                        is org.bukkit.Location -> value.toProxyLocation()
                        is ProxyPlayer -> value.location
                        is Entity -> value.location.toProxyLocation()
                        is Vector -> Location(base?.world, value.x, value.y, value.z)
                        is org.bukkit.util.Vector -> Location(base?.world, value.x, value.y, value.z)
                        is Collection<*> -> {
                            val locations = value.mapNotNull { content ->
                                when (content) {
                                    is Location -> content
                                    is org.bukkit.Location -> content.toProxyLocation()
                                    is ProxyPlayer -> content.location
                                    is Entity -> content.location.toProxyLocation()
                                    is Vector -> Location(base?.world, content.x, content.y, content.z)
                                    is org.bukkit.util.Vector -> Location(base?.world, content.x, content.y, content.z)
                                    else -> null
                                }
                            }

                            for (location in locations) {
                                brush.draw(location, viewers)
                            }

                            return@thenApply
                        }
                        else -> base
                    }

                    brush.draw(location ?: error("No location selected."), viewers)
                }
            }
            else -> return CompletableFuture.completedFuture(false)
        }
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
                    val location = reader.readLocation()
                    actionTake {
                        location.getOrNull(this).thenApply {
                            this.setVariable(ActionCanvas.VARIABLE_ORIGIN, it ?: this.unsafePlayer()?.location)
                            return@thenApply it
                        }
                    }
                }
                "by", "with" -> {
                    ActionDraw(reader.readVector(false))
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