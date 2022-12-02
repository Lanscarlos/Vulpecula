package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.kether.live.readColor
import top.lanscarlos.vulpecula.kether.live.readItemStack
import java.awt.Color

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:43
 */
object ItemColorHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("color")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        reader.hasNextToken("to")
        val next = reader.readColor()

        return acceptTransferFuture(source) { item ->
            next.get(this, Color.WHITE).thenApply { color ->
                val meta = item.itemMeta ?: return@thenApply item

                when (meta) {
                    is LeatherArmorMeta -> meta.setColor(color.toBukkit())
                    is PotionMeta -> meta.color = color.toBukkit()
                }

                return@thenApply item.also { it.itemMeta = meta }
            }
        }
    }

    private fun Color.toBukkit(): org.bukkit.Color {
        return org.bukkit.Color.fromRGB(this.rgb)
    }
}