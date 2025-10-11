package top.lanscarlos.vulpecula.module.property.common

import org.bukkit.inventory.ItemStack
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalProperty
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.module.bacikal.annotation.Property

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property.common
 *
 * @author Lanscarlos
 * @since 2025/10/11
 */
@Property(bind = ItemStack::class)
object ItemStackProperty : BacikalProperty<ItemStack> {

    @Suppress("DEPRECATION")
    override fun readProperty(instance: ItemStack, key: String): Any? {
        return try {
            when(key) {
                "type" -> instance.type
                "amount" -> instance.amount
                "data" -> instance.data
                "durability" -> instance.durability
                "maxStackSize" -> instance.maxStackSize
                "enchantments" -> instance.enchantments
                "itemMeta" -> instance.itemMeta
                "translationKey" -> instance.translationKey
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    @Suppress("DEPRECATION")
    override fun writeProperty(instance: ItemStack, key: String, value: Any?) {
        try {
            when(key) {
                "amount" -> instance.amount = value.let(IntApplicative::convert)
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}