package top.lanscarlos.vulpecula.kether.action.canvas

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.Location
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.action.ActionLocation
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-10 19:29
 */
class ActionParticles(
    val location: LiveData<Location>?,
    val viewers: ParsedAction<*>,
    val options: Map<String, LiveData<*>>
) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {

        val brush = CanvasBrush()
        for (it in options) {
            ActionBrush.modify(brush, frame, it.key, it.value)
        }

        val loc = location.getValue(frame, frame.unsafePlayer()?.location ?: ActionLocation.def)

        val viewers = when (val value = frame.run(this.viewers).join()) {
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
         * particles play at &loc -x -xx
         *
         * */
        @VulKetherParser(
            id = "particles",
            name = ["particles"]
        )
        fun parser() = scriptParser { reader ->
            reader.switch {
                case("play") {

                    val loc = if (reader.hasNextToken("at")) {
                        reader.readLocation()
                    } else {
                        null
                    }

                    val options = mutableMapOf<String, LiveData<*>>()
                    val viewers = mutableSetOf<Any>()

                    while (reader.nextPeek().startsWith('-')) {
                        when (val it = reader.nextToken().substring(1)) {
                            "viewers", "viewer" -> {
                                viewers.addAll(ActionViewers.read(reader))
                            }
                            else -> ActionBrush.read(reader, it, options)
                        }
                    }

                    ActionParticles(loc, ParsedAction(ActionViewers(viewers)), options)
                }
            }
        }
    }
}