package top.lanscarlos.vulpecula.kether.action.effect

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.setVariable
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.effect
 *
 * @author Lanscarlos
 * @since 2022-11-09 00:23
 */
class ActionViewers(val raw: Set<Any>) : ScriptAction<Collection<ProxyPlayer>>() {

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
                    frame.run(value).join()?.let {
                        cache += when (it) {
                            is ProxyPlayer -> it
                            is Player -> adaptPlayer(it)
                            is String -> adaptPlayer(Bukkit.getPlayerExact(it) ?: return@let)
                            else -> return@let
                        }
                    }
                }
            }
        }

        val viewers = cache.distinctBy { it.uniqueId.toString() }
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
            val raw = mutableSetOf<Any>()
            if (reader.hasNextToken("to")) {
                if (reader.hasNextToken("[")) {
                    while (!reader.hasNextToken("]")) {
                        raw += reader.nextBlock()
                    }
                } else {
                    raw += reader.nextBlock()
                }
            } else {
                if (reader.hasNextToken("[")) {
                    while (!reader.hasNextToken("]")) {
                        raw += reader.nextToken()
                    }
                } else {
                    raw += reader.nextToken()
                }
            }

            ActionViewers(raw)
        }
    }
}