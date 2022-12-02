package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.readItemStack

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:42
 */
object ItemDestroyHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("destroy")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null

        return acceptHandleNow(source) { item ->
            item.also { it.amount = 0 }
        }
    }
}