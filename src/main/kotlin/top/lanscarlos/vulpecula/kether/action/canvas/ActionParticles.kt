package top.lanscarlos.vulpecula.kether.action.canvas

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.Location
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.readLocation
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
    val viewers: ParsedAction<*>,
    val options: Map<String, LiveData<*>>
) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        return options.mapValues {
            it.value.getOrNull(frame)
        }.plus("viewers" to frame.run(viewers)).thenTake().thenApply { args ->

            val brush = CanvasBrush()
            val location = args["location"] as? Location ?: frame.playerOrNull()?.location ?: error("No location selected.")
            val viewers = when (val value = args["viewers"]) {
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

            for (option in args) {
                ActionBrush.modify(brush, option.key, option.value)
            }

            brush.draw(location, viewers)
        }
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
                    val options = mutableMapOf<String, LiveData<*>>()

                    if (reader.hasNextToken("at")) {
                        options["location"] = reader.readLocation()
                    }

                    val viewers = mutableSetOf<Any>()

                    while (reader.nextPeek().startsWith('-')) {
                        when (val it = reader.nextToken().substring(1)) {
                            "viewers", "viewer" -> {
                                viewers.addAll(ActionViewers.read(reader))
                            }
                            else -> ActionBrush.read(reader, it, options)
                        }
                    }

                    ActionParticles(ParsedAction(ActionViewers(viewers)), options)
                }
            }
        }
    }
}