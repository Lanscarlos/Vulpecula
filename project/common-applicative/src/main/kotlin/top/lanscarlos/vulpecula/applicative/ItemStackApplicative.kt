package top.lanscarlos.vulpecula.applicative

import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 15:12
 */
class ItemStackApplicative(source: Any) : AbstractApplicative<ItemStack>(source) {

    override fun transfer(source: Any, def: ItemStack?): ItemStack? {
        return when (source) {
            is ItemStack -> source
            is Item -> source.itemStack
            is String -> {
                val material = XMaterial.matchXMaterial(source.uppercase()).let { mat ->
                    if (mat.isPresent) mat.get() else return def
                }
                buildItem(material)
            }

            else -> def
        }
    }

    companion object {

        fun Any.applicativeItemStack() = ItemStackApplicative(this)
    }

}