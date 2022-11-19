package top.lanscarlos.vulpecula.kether.action.canvas

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.setVariable
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-09 00:23
 */
class ActionViewers(val raw: Collection<Any>) : ScriptAction<Collection<ProxyPlayer>>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Collection<ProxyPlayer>> {

        val cache = mutableSetOf<ProxyPlayer>()

        for (value in raw) {
            when (value) {
                "*" -> {
                    cache += onlinePlayers()
                    break
                }
                is String -> {
                    cache += adaptPlayer(Bukkit.getPlayerExact(value) ?: continue)
                }
                is ParsedAction<*> -> {
                    frame.run(value).join()?.let { result ->
                        when (result) {
                            is ProxyPlayer -> cache += result
                            is Player -> cache += adaptPlayer(result)
                            is String -> cache += adaptPlayer(Bukkit.getPlayerExact(result) ?: return@let)
                            is Collection<*> -> {
                                result.forEach {
                                    when (it) {
                                        is ProxyPlayer -> cache += it
                                        is Player -> cache += adaptPlayer(it)
                                        is String -> cache += adaptPlayer(Bukkit.getPlayerExact(it) ?: return@let)
                                    }
                                }
                            }
                            is Array<*> -> {
                                result.forEach {
                                    when (it) {
                                        is ProxyPlayer -> cache += it
                                        is Player -> cache += adaptPlayer(it)
                                        is String -> cache += adaptPlayer(Bukkit.getPlayerExact(it) ?: return@let)
                                    }
                                }
                            }
                            else -> return@let
                        }
                    }
                }
            }
        }

        val viewers = if (cache.isNotEmpty()) {
            cache.distinctBy { it.uniqueId.toString() }
        } else {
            listOf(frame.player())
        }
        frame.setVariable(ActionCanvas.VARIABLE_VIEWERS, viewers)
        return CompletableFuture.completedFuture(viewers)
    }

    companion object {

        /**
         *
         * 所有在线玩家
         * viewers *
         *
         * 指定玩家名字
         * viewers Lanscarlos
         * viewers [ Lanscarlos Tony ]
         *
         * 从 action 中获取玩家或其名字
         * viewers to {action}
         * viewers to target select EIR -r 6
         *
         * viewers to [ {actions...} ]
         * viewers to [ player name literal Lanscarlos ]
         *
         * */
        @VulKetherParser(
            id = "viewers",
            name = ["viewers", "viewer"],
            namespace = "vulpecula-canvas"
        )
        fun parser() = scriptParser { reader ->
            ActionViewers(read(reader))
        }

        fun read(reader: QuestReader): Collection<Any> {
            val viewers = mutableSetOf<Any>()
            if (reader.hasNextToken("to")) {
                if (reader.hasNextToken("[")) {
                    while (!reader.hasNextToken("]")) {
                        viewers += reader.nextBlock()
                    }
                } else {
                    viewers += reader.nextBlock()
                }
            } else {
                if (reader.hasNextToken("[")) {
                    while (!reader.hasNextToken("]")) {
                        viewers += reader.nextToken()
                    }
                } else {
                    viewers += reader.nextToken()
                }
            }
            return viewers
        }
    }
}