package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.readInt
import top.lanscarlos.vulpecula.kether.live.readItemStack
import top.lanscarlos.vulpecula.utils.coerceInt
import top.lanscarlos.vulpecula.utils.duraFix
import top.lanscarlos.vulpecula.utils.maxDurability

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
        when (val next = reader.nextToken()) {
            "fix", "set", "damage", "dmg" -> {
                val amount = reader.readInt()

                return acceptTransferFuture(source) { item ->
                    amount.getOrNull(this).thenApply {
                        item.duraFix = when (next) {
                            "fix" -> item.duraFix - it.coerceInt(0)
                            "damage", "dmg" -> item.duraFix + it.coerceInt(0)
                            "set" -> it.coerceInt(item.duraFix)
                            else -> item.duraFix
                        }.coerceIn(0, item.maxDurability)

                        return@thenApply item
                    }
                }
            }
            "current", "cur", "max" -> {
                return acceptHandleNow(source) { item ->
                    when (next) {
                        "current", "cur" -> item.duraFix
                        "max" -> item.maxDurability
                        else -> -1
                    }
                }
            }
            else -> {
                // 显示耐久
                reader.reset()
                return acceptHandleNow(source) { item -> item.duraFix }
            }
        }
    }
}