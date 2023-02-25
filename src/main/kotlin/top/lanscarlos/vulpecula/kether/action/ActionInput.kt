package top.lanscarlos.vulpecula.kether.action

import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common5.clong
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.buildParser
import top.lanscarlos.vulpecula.utils.playerOrNull
import top.lanscarlos.vulpecula.utils.toBukkit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2023-02-25 14:23
 */
object ActionInput {

    val chat = ConcurrentHashMap<Player, CompletableFuture<String>>()
    val sign = mutableMapOf<Player, CompletableFuture<String>>()
    val anvil = mutableMapOf<Player, CompletableFuture<String>>()
    val book = mutableMapOf<Player, CompletableFuture<String>>()

    @SubscribeEvent
    fun e(e: AsyncPlayerChatEvent) {
        if (!chat.containsKey(e.player)) return
        val future = chat.remove(e.player)!!
        if (future.isDone) return
        future.complete(e.message)
        e.isCancelled = true
    }

    @VulKetherParser(
        id = "input",
        name = ["input"]
    )
    fun parser() = buildParser {
        group(
            option("from", "by", then = string("chat"), def = "chat"),
            arguments(
                arrayOf("timeout", "time") to long(1200)
            )
        ) { type, options ->
            future {
                val player = this.playerOrNull()?.toBukkit() ?: error("No player selected.")
                val future = when (type.lowercase()) {
                    "chat" -> chat.computeIfAbsent(player) { CompletableFuture() }
                    "sign" -> sign.computeIfAbsent(player) { CompletableFuture() }
                    "anvil" -> anvil.computeIfAbsent(player) { CompletableFuture() }
                    "book" -> book.computeIfAbsent(player) { CompletableFuture() }
                    else -> error("Input type \"$type\" is not supported yet.")
                }

                // 额外参数
                for ((key, value) in options) {
                    when (key) {
                        "timeout" -> {
                            // 设置超时
                            val timeout = value.clong
                            if (timeout <= 0) continue
                        }
                    }
                }

                // 设置超时
                val timeout = options["timeout"]?.clong ?: 1200
                if (timeout > 0) {
                    submit(delay = timeout) { future.complete(null) }
                }

                return@future future
            }
        }
    }

}