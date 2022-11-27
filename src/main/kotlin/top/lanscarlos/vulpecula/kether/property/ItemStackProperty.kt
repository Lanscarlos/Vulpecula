package top.lanscarlos.vulpecula.kether.property

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.material.MaterialData
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty
import top.lanscarlos.vulpecula.utils.coerceBoolean
import top.lanscarlos.vulpecula.utils.coerceInt
import top.lanscarlos.vulpecula.utils.coerceShort

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property
 *
 * @author Lanscarlos
 * @since 2022-11-13 13:29
 */

@VulKetherProperty(
    id = "itemstack",
    bind = ItemStack::class
)
class ItemStackProperty : VulScriptProperty<ItemStack>("itemstack") {
    override fun readProperty(instance: ItemStack, key: String): OpenResult {
        val property: Any? = when (key) {
            "clone" -> instance.clone()
            "amount", "amt" -> instance.amount
            "data" -> instance.data
            "durability", "dura" -> instance.durability
            "enchantments" -> instance.enchantments
            "item-meta", "meta" -> instance.itemMeta
            "max-stack-size", "max-size" -> instance.maxStackSize
            "type*" -> instance.type
            "type", "material", "mat" -> instance.type.name
            "has-meta" -> instance.hasItemMeta()
            "serialize" -> instance.serialize()
            "to-string", "string" -> instance.toString()

            "name" -> instance.itemMeta?.displayName
            "has-name" -> instance.itemMeta?.hasDisplayName()
            "lore" -> instance.itemMeta?.lore
            "has-lore" -> instance.itemMeta?.hasLore()
            "enchants" -> instance.itemMeta?.enchants
            "has-enchantments", "has-enchants" -> instance.itemMeta?.hasEnchants()
            "flags" -> instance.itemMeta?.itemFlags
            "custom-model-data", "custom-model", "model-data", "model" -> instance.itemMeta?.customModelData
            "has-custom-model-data", "has-custom-model", "has-model-data", "has-model" -> instance.itemMeta?.hasCustomModelData()
//            "attribute-modifiers", "modifiers" -> instance.itemMeta?.attributeModifiers
            "has-attribute-modifiers", "has-modifiers" -> instance.itemMeta?.hasAttributeModifiers()
            "unbreakable", "unbreak" -> instance.itemMeta?.isUnbreakable
            "localized-name", "localized" -> instance.itemMeta?.localizedName
            "has-localized-name", "has-localized" -> instance.itemMeta?.hasLocalizedName()
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: ItemStack, key: String, value: Any?): OpenResult {
        when (key) {
            "amount", "amt" -> {
                instance.amount = value.coerceInt(instance.amount)
            }
            "data" -> {
                instance.data = value as? MaterialData
            }
            "durability", "dura" -> {
                instance.durability = value.coerceShort(instance.durability)
            }
            "item-meta", "meta" -> {
                instance.itemMeta = value as? ItemMeta
            }
            "type" -> {
                instance.type = when (value) {
                    is Material -> value
                    is String -> Material.values().firstOrNull { value.equals(it.name, true) } ?: instance.type
                    else -> instance.type
                }
            }

            "name" -> {
                val meta = instance.itemMeta
                meta?.setDisplayName(value?.toString())
                instance.itemMeta = meta
            }
            "lore" -> {
                val meta = instance.itemMeta
                meta?.lore = when (value) {
                    is String -> listOf(value)
                    is Array<*> -> value.mapNotNull { it?.toString() }
                    is Collection<*> -> value.mapNotNull { it?.toString() }
                    else -> null
                }
                instance.itemMeta = meta
            }
            "custom-model-data", "custom-model", "model-data", "model" -> {
                val meta = instance.itemMeta
                meta?.setCustomModelData(value.coerceInt(meta.customModelData))
                instance.itemMeta = meta
            }
            "unbreakable", "unbreak" -> {
                instance.itemMeta?.isUnbreakable = value.coerceBoolean(instance.itemMeta?.isUnbreakable)
            }
            "localized-name", "localized" -> {
                instance.itemMeta?.setLocalizedName(value?.toString())
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}