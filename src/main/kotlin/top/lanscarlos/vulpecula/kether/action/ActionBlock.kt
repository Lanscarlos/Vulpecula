package top.lanscarlos.vulpecula.kether.action

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.hasNextToken
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-06 20:21
 */
class ActionBlock(
    val block: List<ParsedAction<*>>
) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        if (block.isEmpty()) {
            return CompletableFuture.completedFuture(null)
        } else if (block.size == 1) {
            return frame.run(block[0])
        } else {
            var previous: CompletableFuture<Any?> = frame.run(block.first())
            for (i in 1 until block.size) {
                previous = handle(previous, frame, block[i])
            }
            return previous
        }
    }

    fun handle(previous: CompletableFuture<Any?>, frame: ScriptFrame, action: ParsedAction<*>): CompletableFuture<Any?> {
        if (previous.isDone) {
            return frame.run(action)
        } else {
            val future = CompletableFuture<Any?>()
            previous.thenRun {
                frame.run(action).thenAccept { future.complete(it) }
            }
            return future
        }
    }



    companion object {
        @VulKetherParser(
            id = "block",
            name = ["blockof"]
        )
        fun parser() = scriptParser { reader ->
            reader.expect("{")
            ActionBlock(readBlock(reader))
        }

        fun readBlock(reader: QuestReader): List<ParsedAction<*>> {
            val block = mutableListOf<ParsedAction<*>>()
            while (!reader.hasNextToken("}")) {
                block += reader.nextParsedAction()
            }
            return block
        }
    }
}