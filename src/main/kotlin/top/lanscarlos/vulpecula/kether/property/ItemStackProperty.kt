package top.lanscarlos.vulpecula.kether.property

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.material.MaterialData
import taboolib.common.OpenResult
import taboolib.module.nms.MinecraftVersion
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
@SuppressWarnings("deprecation")
class ItemStackProperty : VulScriptProperty<ItemStack>("itemstack") {
    override fun readProperty(instance: ItemStack, key: String): OpenResult {
        val property: Any? = when (key) {
            "type", "material", "mat" -> instance.type.name
            "name" -> instance.itemMeta?.displayName
            "has-name" -> instance.itemMeta?.hasDisplayName()

            "lore" -> instance.itemMeta?.lore
            "has-lore" -> instance.itemMeta?.hasLore()

            "amount", "amt" -> instance.amount
            "max-amount", "max-amt", "max-size" -> instance.maxStackSize

            "durability", "dura" -> {
                // 1.13+
                if (MinecraftVersion.major >= 5) {
                    val damage = (instance.itemMeta as? Damageable)?.damage ?: 0
                    instance.type.maxDurability - damage
                } else {
                    instance.type.maxDurability - instance.durability
                }
            }
            "damage", "dmg" -> {
                // 1.13+
                if (MinecraftVersion.major >= 5) {
                    (instance.itemMeta as? Damageable)?.damage ?: 0
                } else {
                    instance.durability.toInt()
                }
            }
            "max-durability", "max-dura" -> instance.type.maxDurability.toInt()

            "enchants" -> instance.itemMeta?.enchants
            "enchantments" -> instance.enchantments
            "has-enchantments", "has-enchants" -> instance.itemMeta?.hasEnchants()

            "flags" -> instance.itemMeta?.itemFlags
            "has-flags" -> instance.itemMeta?.itemFlags?.isNotEmpty() ?: false

            "custom-model-data", "custom-model", "model-data", "model" -> instance.itemMeta?.customModelData
            "has-custom-model-data", "has-custom-model", "has-model-data", "has-model" -> instance.itemMeta?.hasCustomModelData()

//            "attribute-modifiers", "modifiers" -> instance.itemMeta?.attributeModifiers
            "has-attribute-modifiers", "has-modifiers" -> instance.itemMeta?.hasAttributeModifiers()

            "unbreakable" -> instance.itemMeta?.isUnbreakable

            "item-meta", "meta" -> instance.itemMeta
            "has-meta" -> instance.hasItemMeta()

            /* 材质相关属性 */
            "is-solid" -> instance.type.isSolid
            "is-item" -> instance.type.isItem
            "is-record" -> instance.type.isRecord
            "is-occluding" -> instance.type.isOccluding
            "is-interactable" -> instance.type.isInteractable
            "is-fuel" -> instance.type.isFuel
            "is-flammable" -> instance.type.isFlammable
            "is-edible" -> instance.type.isEdible
            "is-burnable" -> instance.type.isBurnable
            "is-block" -> instance.type.isBlock
            "is-air" -> instance.type.isAir
            "has-gravity" -> instance.type.hasGravity()
            "slipperiness" -> instance.type.slipperiness
            "hardness" -> instance.type.hardness
            "slot" -> instance.type.equipmentSlot.name
            "blast-resistance", "resistance" -> instance.type.blastResistance
            "creative-category", "category" -> instance.type.creativeCategory

            /* 其他不常用属性 */
            "clone" -> instance.clone()
            "data" -> instance.data
            "serialize" -> instance.serialize()
            "to-string", "string" -> instance.toString()
            "localized-name", "localized" -> instance.itemMeta?.localizedName
            "has-localized-name", "has-localized" -> instance.itemMeta?.hasLocalizedName()
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: ItemStack, key: String, value: Any?): OpenResult {
        when (key) {
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
            "amount", "amt" -> {
                instance.amount = value.coerceInt(instance.amount)
            }
            "durability", "dura" -> {
                // 1.13+
                if (MinecraftVersion.major >= 5) {
                    (instance.itemMeta as? Damageable)?.damage = value?.coerceInt() ?: return OpenResult.successful()
                } else {
                    instance.durability = value?.coerceShort() ?: return OpenResult.successful()
                }
            }
            "item-meta", "meta" -> {
                instance.itemMeta = value as? ItemMeta
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