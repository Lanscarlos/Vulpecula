package top.lanscarlos.vulpecula.kether.action

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.run
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
            val future = CompletableFuture<Any?>()
            val futures = block.map { frame.run(it) }
            val counter = AtomicInteger(0)
            for (it in futures) {
                if (it.isDone) {
                    val count = counter.incrementAndGet()

                    // 判断 futures 是否全部执行完毕
                    if (count >= futures.size) {
                        future.complete(futures.last().getNow(null))
                    }
                } else {
                    it.thenRun {
                        val count = counter.incrementAndGet()

                        // 判断 futures 是否全部执行完毕
                        if (count >= futures.size) {
                            future.complete(futures.last().getNow(null))
                        }
                    }
                }
            }
            return future
        }
    }

    companion object {
        @VulKetherParser(
            id = "block",
            name = ["block"]
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