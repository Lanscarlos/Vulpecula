package top.lanscarlos.vulpecula.bacikal.action

import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.nms.inputSign
import top.lanscarlos.vulpecula.bacikal.bacikal
import top.lanscarlos.vulpecula.kether.VulKetherParser
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
    fun parser() = bacikal {
        combineOf(
            optional("from", "by", then = text("chat"), def = "chat"),
            argument("timeout", "time", then = int(1200), def = 1200)
        ) { type, timeout ->
            val player = this.playerOrNull()?.toBukkit() ?: error("No player selected.")
            val future = when (type.lowercase()) {
                "chat" -> chat.computeIfAbsent(player) { CompletableFuture() }
                "sign" -> {
                    val future = CompletableFuture<Array<String>>()
                    player.inputSign { future.complete(it) }
                    future
                }
                "anvil" -> anvil.computeIfAbsent(player) { CompletableFuture() }
                "book" -> book.computeIfAbsent(player) { CompletableFuture() }
                else -> error("Input type \"$type\" is not supported yet.")
            }

            if (timeout > 0) {
                submit(delay = timeout.toLong()) { future.complete(null) }
            }

            return@combineOf future
        }
    }

}