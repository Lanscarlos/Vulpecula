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
@Deprecated("迁移完毕")
class ActionBlock(
    val block: List<ParsedAction<*>>
) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        return if (block.isEmpty()) {
            CompletableFuture.completedFuture(null)
        } else if (block.size == 1) {
            frame.run(block[0])
        } else {
            var previous: CompletableFuture<Any?> = frame.run(block.first())
            for (i in 1 until block.size) {
                previous = handle(previous, frame, block[i])
            }
            previous
        }
    }

    fun handle(
        previous: CompletableFuture<Any?>,
        frame: ScriptFrame,
        action: ParsedAction<*>
    ): CompletableFuture<Any?> {
        return if (previous.isDone) {
            frame.run(action)
        } else {
            previous.thenCompose { frame.run(action) }
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