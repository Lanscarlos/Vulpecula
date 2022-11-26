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
import top.lanscarlos.vulpecula.utils.thenTake
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-09 00:23
 */
class ActionViewers(val raw: Collection<Any>) : ScriptAction<Collection<ProxyPlayer>>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Collection<ProxyPlayer>> {
        val future = CompletableFuture<Collection<ProxyPlayer>>()
        val viewers = mutableSetOf<ProxyPlayer>()
        val wait = mutableSetOf<CompletableFuture<*>>()

        // 处理原始数据
        for (value in raw) {
            when (value) {
                '*', "*" -> {
                    viewers += onlinePlayers()
                    break
                }
                is String -> {
                    viewers += adaptPlayer(Bukkit.getPlayerExact(value) ?: continue)
                }
                is ParsedAction<*> -> {
                    wait += frame.run(value)
                }
            }
        }

        if (wait.isNotEmpty()) {
            // 处理等待列表
            val counter = AtomicInteger(0)
            for (it in wait) {
                if (it.isDone) {
                    viewers.tryPlus(it.getNow(null))

                    val count = counter.incrementAndGet()
                    // 判断等待队列是否全部执行完毕
                    if (!future.isDone && count >= wait.size) {
                        future.complete(viewers)
                    }
                } else {
                    it.thenAccept { value ->
                        viewers.tryPlus(value)

                        val count = counter.incrementAndGet()
                        // 判断等待队列是否全部执行完毕
                        if (!future.isDone && count >= wait.size) {
                            future.complete(viewers)
                        }
                    }
                }
            }

            return future.thenApply {
                frame.setVariable(ActionCanvas.VARIABLE_VIEWERS, viewers)

                return@thenApply if (it.isNotEmpty()) {
                    it.distinctBy { player -> player.uniqueId.toString() }
                } else {
                    listOf(frame.player())
                }
            }
        } else {
            frame.setVariable(ActionCanvas.VARIABLE_VIEWERS, viewers)
            future.complete(
                if (viewers.isNotEmpty()) {
                    viewers.distinctBy { player -> player.uniqueId.toString() }
                } else {
                    listOf(frame.player())
                }
            )

            return future
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