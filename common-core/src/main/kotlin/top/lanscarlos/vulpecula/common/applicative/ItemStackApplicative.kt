package top.lanscarlos.vulpecula.common.applicative

import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.warning
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem
import top.lanscarlos.vulpecula.common.applicative.exception.ValueConversionException
import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 15:12
 */
object ItemStackApplicative : AbstractApplicative<ItemStack>(ItemStack::class.java) {

    override val aliases: Array<String> = arrayOf("item")

    override fun convertOrThrow(instance: Any): ItemStack {
        return when (instance) {
            is ItemStack -> instance
            is Item -> instance.itemStack
            is String -> {
                val material = XMaterial.matchXMaterial(instance.uppercase()).let { mat ->
                    if (mat.isPresent) {
                        mat.get()
                    } else {
                        warning("ItemStackApplicative#apply >> Instance cannot transform to material. $instance")
                        throw ValueConversionException(instance, ItemStack::class.java)
                    }
                }
                buildItem(material)
            }
            else -> throw TypeConversionException(instance::class.java, ItemStack::class.java)
        }
    }

}