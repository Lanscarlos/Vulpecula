package top.lanscarlos.vulpecula.kether.action

import taboolib.common.platform.function.info
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.run
import java.util.concurrent.CompletableFuture

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
            for (i in 0 until block.lastIndex) {
                block[i].run(frame)
            }
        }
        return frame.run(block[block.lastIndex])
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