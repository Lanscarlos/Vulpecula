package top.lanscarlos.vulpecula.kether.live

import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.library.xseries.XMaterial
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.platform.util.buildItem
import top.lanscarlos.vulpecula.utils.nextBlock

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * @author Lanscarlos
 * @since 2022-11-12 14:24
 */
class ItemLiveData(
    val value: Any
) : LiveData<ItemStack> {

    override fun get(frame: ScriptFrame, def: ItemStack): ItemStack {
        val it = if (value is ParsedAction<*>) {
            frame.run(value).join()
        } else value

        return when (it) {
            is ItemStack -> it
            is Item -> it.itemStack
            is String -> {
                val material = XMaterial.matchXMaterial(it.uppercase()).let { mat ->
                    if (mat.isPresent) mat.get() else return def
                }
                buildItem(material)
            }
            else -> def
        }
    }

    override fun getOrNull(frame: ScriptFrame): ItemStack? {
        val it = if (value is ParsedAction<*>) {
            frame.run(value).join()
        } else value

        return when (it) {
            is ItemStack -> it
            is Item -> it.itemStack
            is String -> {
                val material = XMaterial.matchXMaterial(it.uppercase()).let { mat ->
                    if (mat.isPresent) mat.get() else return null
                }
                buildItem(material)
            }
            else -> null
        }
    }

    companion object {
        fun read(reader: QuestReader): LiveData<ItemStack> {
            return ItemLiveData(reader.nextBlock())
        }
    }
}