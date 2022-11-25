package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.readInt
import top.lanscarlos.vulpecula.utils.readItemStack
import top.lanscarlos.vulpecula.utils.toInt

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
        when (val next = reader.nextToken()) {
            "add", "give",
            "sub", "take",
            "set" -> {
                reader.hasNextToken("to")
                val amount = reader.readInt()

                return acceptTransferFuture(source) { item ->
                    amount.getOrNull(this).thenApply {
                        item.amount = when (next) {
                            "add", "give" -> item.amount + it.toInt(0)
                            "sub", "take" -> item.amount - it.toInt(0)
                            "set" -> it.toInt(item.amount)
                            else -> item.amount
                        }.coerceIn(0, item.maxStackSize)

                        return@thenApply item
                    }
                }
            }
            "current", "cur", "max" -> {
                return acceptHandleNow(source) { item ->
                    when (next) {
                        "current", "cur" -> item.amount
                        "max" -> item.maxStackSize
                        else -> -1
                    }
                }
            }
            else -> {
                reader.reset()
                return acceptHandleNow(source) { item -> item.amount }
            }
        }
    }
}