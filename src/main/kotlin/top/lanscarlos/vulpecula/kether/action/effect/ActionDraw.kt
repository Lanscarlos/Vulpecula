package top.lanscarlos.vulpecula.kether.action.effect

import org.bukkit.block.Block
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
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.effect
 *
 * @author Lanscarlos
 * @since 2022-11-09 00:20
 */
class ActionDraw(val raw: Any) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {

        val base = frame.getVariable<Location>("@DrawBase") ?: frame.player().location

        val loc = when (raw) {
            is ParsedAction<*> -> {
                when (val it = frame.run(raw).join()) {
                    is Location -> {
                        it.clone()
                    }
                    is org.bukkit.Location -> it.toProxyLocation().clone()
                    is Entity -> it.location.toProxyLocation().clone()
                    is Block -> it.location.toProxyLocation().clone()
                    is Vector -> base.clone().add(it.x, it.y, it.z)
                    is org.bukkit.util.Vector -> base.clone().add(it.x, it.y, it.z)
                    else -> base
                }
            }
            is Triple<*, *, *> -> {
                val x = when (val it = raw.first) {
                    is Double -> it
                    is ParsedAction<*> -> frame.coerceDouble(it, base.x)
                    else -> base.x
                }
                val y = when (val it = raw.second) {
                    is Double -> it
                    is ParsedAction<*> -> frame.coerceDouble(it, base.y)
                    else -> base.y
                }
                val z = when (val it = raw.third) {
                    is Double -> it
                    is ParsedAction<*> -> frame.coerceDouble(it, base.z)
                    else -> base.z
                }

                base.clone().add(x, y, z)
            }
            else -> base
        }

        val brush = frame.getVariable<CanvasBrush>("@Brush") ?: CanvasBrush().also {
            frame.setVariable("@Brush", it)
        }

        val viewers = when (val value = frame.getVariable<Any>("@Viewers")) {
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
         * draw line [from &loc] to &loc
         * draw circle [at &loc] -r 3.5
         * draw cube
         *
         * 定义基准点, 即原点
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
         *
         * */
        @VulKetherParser(
            id = "draw",
            name = ["draw", "draw"],
            namespace = "vulpecula-canvas"
        )
        fun parser() = scriptParser { reader ->
            reader.mark()
            when (val next = reader.nextToken()) {
                "base" -> {
                    val raw = reader.nextBlock()
                    actionNow {
                        this.setVariable("@DrawBase", this.coerceLocation(raw, this.player().location))
                    }
                }
                "at", "by", "with" -> {
                    ActionDraw(reader.nextBlock())
                }
                else -> {
                    val x = next.toDoubleOrNull() ?: let {
                        reader.reset()
                        reader.nextBlock()
                    }
                    reader.mark()
                    val y = reader.nextToken().toDoubleOrNull() ?: let {
                        reader.reset()
                        reader.nextBlock()
                    }
                    reader.mark()
                    val z = reader.nextToken().toDoubleOrNull() ?: let {
                        reader.reset()
                        reader.nextBlock()
                    }
                    ActionDraw(Triple(x, y, z))
                }
            }
        }
    }
}