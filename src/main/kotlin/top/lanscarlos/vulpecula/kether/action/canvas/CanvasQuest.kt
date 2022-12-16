package top.lanscarlos.vulpecula.kether.action.canvas

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.Quest
import java.util.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-08 00:32
 */
class CanvasQuest(
    val uniqueId: String,
    val period: Int,
    val condition: ParsedAction<*>,
    val body: ParsedAction<*>,
    val preHandle: ParsedAction<*>,
    val postHandle: ParsedAction<*>
) : Quest {

    val block = CanvasBlock(listOf(preHandle, body, postHandle))

    override fun getId(): String {
        return uniqueId
    }

    override fun getBlock(label: String): Optional<Quest.Block> {
        return Optional.of(block)
    }

    override fun getBlocks(): MutableMap<String, Quest.Block> {
        return mutableMapOf("def" to block)
    }

    override fun blockOf(action: ParsedAction<*>): Optional<Quest.Block> {
        return if (action in block.queue) return Optional.of(block) else Optional.empty()
    }

    class CanvasBlock(
        val queue: List<ParsedAction<*>>
    ) : Quest.Block {

        override fun getLabel(): String {
            return "def"
        }

        override fun getActions(): MutableList<ParsedAction<*>> {
            return queue.toMutableList()
        }

        override fun indexOf(action: ParsedAction<*>): Int {
            return queue.indexOf(action)
        }

        override fun get(index: Int): Optional<ParsedAction<*>> {
            return if (index >=0 && index < queue.size) {
                Optional.of(queue[index])
            } else {
                Optional.empty()
            }
        }

    }
}