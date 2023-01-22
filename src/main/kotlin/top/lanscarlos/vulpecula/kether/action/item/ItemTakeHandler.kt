package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.readString
import top.lanscarlos.vulpecula.kether.live.tryReadInt
import top.lanscarlos.vulpecula.utils.chemdah.InferItem.Companion.toInferItem
import top.lanscarlos.vulpecula.utils.coerceInt
import top.lanscarlos.vulpecula.utils.playerOrNull
import top.lanscarlos.vulpecula.utils.thenTake
import top.lanscarlos.vulpecula.utils.toBukkit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2023-01-23 00:29
 */
object ItemTakeHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("take", "remove")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val pattern = reader.readString()
        val amount = reader.tryReadInt("amount", "amt")

        return handleFuture {
            listOf(
                pattern.getOrNull(this),
                amount?.getOrNull(this)
            ).thenTake().thenApply { args ->
                val player = this.playerOrNull()?.toBukkit() ?: error("No player selected.")
                val item = args[0].toString().toInferItem()
                val amt = args[1].coerceInt(1)

                if (item.check(player.inventory, amt)) {
                    // 检查背包是否拥有足够的物品
                    item.take(player.inventory, amt)
                } else {
                    false
                }
            }
        }
    }
}