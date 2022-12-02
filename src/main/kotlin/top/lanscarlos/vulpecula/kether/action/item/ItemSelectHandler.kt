package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.readItemStack

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:31
 */
object ItemSelectHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("select", "sel")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val itemStack = if (isRoot) reader.readItemStack() else null
        return acceptTransferNow(itemStack) { item -> item }
    }
}