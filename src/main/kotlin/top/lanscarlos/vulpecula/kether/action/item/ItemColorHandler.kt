package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.readColor
import top.lanscarlos.vulpecula.utils.readItemStack
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
        val raw = reader.readColor()

        return applyTransfer(source) { _, meta ->

            val color = raw.get(this, Color.WHITE).toBukkit()

            when (meta) {
                is LeatherArmorMeta -> meta.setColor(color)
                is PotionMeta -> meta.color = color
            }

            return@applyTransfer meta
        }
    }

    private fun Color.toBukkit(): org.bukkit.Color {
        return org.bukkit.Color.fromRGB(this.rgb)
    }
}