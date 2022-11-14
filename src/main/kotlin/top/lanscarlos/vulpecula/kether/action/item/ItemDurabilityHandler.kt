package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.utils.readInt
import top.lanscarlos.vulpecula.utils.readItemStack

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:35
 */
object ItemDurabilityHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("durability", "dura")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        reader.mark()
        return when (val it = reader.nextToken()) {
            "fix", "set", "damage", "dmg" -> {
                val amount = reader.readInt()
                acceptTransfer(source) { item ->
                    item.durability = when (it) {
                        "fix" -> item.durability - amount.get(this, 0)
                        "damage", "dmg" -> item.durability + amount.get(this, 0)
                        "set" -> amount.get(this, item.durability.toInt())
                        else -> item.durability
                    }.toShort().coerceIn(0, item.type.maxDurability)
                    return@acceptTransfer item
                }
            }
            "current", "cur", "max" -> {
                acceptHandler(source) { item ->
                    when (it) {
                        "current", "cur" -> item.durability
                        "max" -> item.type.maxDurability
                        else -> -1
                    }
                }
            }
            else -> {
                // 显示耐久
                reader.reset()
                acceptHandler(source) { item -> item.durability }
            }
        }
    }
}