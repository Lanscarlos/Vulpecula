package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.utils.readInt
import top.lanscarlos.vulpecula.utils.readItemStack

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:39
 */
object ItemConsumeHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("consume")

    override fun read(isRoot: Boolean, reader: QuestReader): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        val amount = reader.readInt()
        return acceptTransfer(source) { item ->
            val amt = amount.get(this, 0)
            item.amount = (item.amount - amt).coerceIn(0, item.maxStackSize)
            item
        }
    }
}