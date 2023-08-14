package top.lanscarlos.vulpecula.bacikal.action.canvas

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.bacikal.*
import top.lanscarlos.vulpecula.utils.setVariable
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-09 00:23
 */
object ActionViewers {

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
    @BacikalParser(
        id = "viewers",
        aliases = ["viewers", "viewer"],
        namespace = "vulpecula-canvas"
    )
    fun parser() = bacikal {
        combine(
            viewers()
        ) { viewers ->
            this.setVariable(ActionCanvas.VARIABLE_VIEWERS, viewers)
            viewers
        }
    }

    fun viewers(): LiveData<Collection<ProxyPlayer>> {
        return LiveData {
            val cache = mutableSetOf<Any>()
            if (this.expectToken("[")) {
                while (!this.expectToken("]")) {
                    cache += this.readAction()
                }
            } else {
                cache += this.readAction()
            }

            Bacikal.Action { frame ->
                val viewers = mutableSetOf<ProxyPlayer>()
                val wait = mutableListOf<CompletableFuture<*>>()

                // 处理原始数据
                for (value in cache) {
                    when (value) {
                        '*', "*" -> {
                            viewers += onlinePlayers()
                            break
                        }
                        is String -> {
                            if (value[0] == '!') {
                                val exclude = value.substring(1)
                                viewers.removeIf { it.name.equals(exclude, true) }
                            } else {
                                viewers += adaptPlayer(Bukkit.getPlayerExact(value) ?: continue)
                            }
                        }
                        is ParsedAction<*> -> {
                            wait += frame.run(value)
                        }
                    }
                }

                // 处理等待数据
                if (wait.isEmpty()) {
                    return@Action CompletableFuture.completedFuture(viewers)
                } else {
                    return@Action wait.union().thenApply {
                        viewers.tryPlus(it)
                        viewers
                    }
                }
            }
        }
    }

    private fun MutableCollection<ProxyPlayer>.tryPlus(value: Any?) {
        if (value == null) return
        when (value) {
            is ProxyPlayer -> this += value
            is Player -> this += adaptPlayer(value)
            is String -> this += adaptPlayer(Bukkit.getPlayerExact(value) ?: return)
            is Collection<*> -> {
                value.forEach {
                    when (it) {
                        is ProxyPlayer -> this += it
                        is Player -> this += adaptPlayer(it)
                        is String -> this += adaptPlayer(Bukkit.getPlayerExact(it) ?: return)
                    }
                }
            }
            is Array<*> -> {
                value.forEach {
                    when (it) {
                        is ProxyPlayer -> this += it
                        is Player -> this += adaptPlayer(it)
                        is String -> this += adaptPlayer(Bukkit.getPlayerExact(it) ?: return)
                    }
                }
            }
        }
    }
}