package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.readInt
import top.lanscarlos.vulpecula.utils.readItemStack

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:37
 */
object ItemAmountHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("amount", "amt")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        reader.mark()
        return when (val it = reader.nextToken()) {
            "add", "give",
            "sub", "take",
            "set" -> {
                reader.hasNextToken("to")
                val amount = reader.readInt()
                acceptTransfer(source) { item ->
                    item.amount = when (it) {
                        "add", "give" -> item.amount + amount.get(this, 0)
                        "sub", "take" -> item.amount - amount.get(this, 0)
                        "set" -> amount.get(this, item.amount)
                        else -> item.amount
                    }.coerceIn(0, item.maxStackSize)
                    item
                }
            }
            "current", "cur", "max" -> {
                acceptHandler(source) { item ->
                    when (it) {
                        "current", "cur" -> item.amount
                        "max" -> item.maxStackSize
                        else -> -1
                    }
                }
            }
            else -> {
                reader.reset()
                acceptHandler(source) { item -> item.amount }
            }
        }
    }
}