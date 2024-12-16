package top.lanscarlos.vulpecula.common.applicative

import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.warning
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem
import kotlin.jvm.optionals.getOrNull

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 15:12
 */
object ItemStackApplicative : AbstractApplicative<ItemStack>() {

    override fun apply(instance: Any?): ItemStack? {
        return when (instance) {
            is ItemStack -> instance
            is Item -> instance.itemStack
            is String -> {
                val material = XMaterial.matchXMaterial(instance.uppercase()).let { mat ->
                    if (mat.isPresent) {
                        mat.get()
                    } else {
                        warning("ItemStackApplicative#apply >> Instance cannot transform to material. $instance")
                        return null
                    }
                }
                buildItem(material)
            }
            else -> null
        }
    }

    @SuppressWarnings("deprecation")
    override fun readProperty(instance: ItemStack, key: String): Any? {
        return when (key) {
            "type" -> instance.type.name
            "type*" -> instance.type
            "amount" -> instance.amount
            "durability" -> instance.durability
            "maxStackSize" -> instance.maxStackSize
            "maxDurability" -> instance.type.maxDurability
            "displayName" -> instance.itemMeta?.displayName
            "lore" -> instance.itemMeta?.lore
            "enchants" -> instance.itemMeta?.enchants
            "flags" -> instance.itemMeta?.itemFlags
            "unbreakable" -> instance.itemMeta?.isUnbreakable
            "customModelData" -> instance.itemMeta?.customModelData
            "itemFlags" -> instance.itemMeta?.itemFlags
            "itemMeta" -> instance.itemMeta
            else -> failedByGetPropertyNotSupported(instance, key)
        }
    }

    @SuppressWarnings("deprecation")
    override fun writeProperty(instance: ItemStack, key: String, value: Any?) {
        when (key) {
            "type" -> {
                val material = XMaterial.matchXMaterial(value.toString().uppercase()).getOrNull()?.parseMaterial() ?: failedByInvalidValue(instance, key, value)
                instance.type = material
            }
            "amount" -> instance.amount = value.applicativeInt()
            "durability" -> instance.durability = value.applicativeInt().toShort()
            "displayName" -> instance.itemMeta?.setDisplayName(value.toString())
            "lore" -> instance.itemMeta?.lore = value.applicativeStringList()
            "enchants" -> {
                val meta = instance.itemMeta!!
                for (enchant in meta.enchants.keys) {
                    meta.removeEnchant(enchant)
                }
                val enchants = value as? Map<*, *> ?: failedByInvalidValue(instance, key, value)
                for ((name, level) in enchants) {
                    meta.addEnchant(Enchantment.getByName(name.toString()) ?: continue, level.applicativeInt(), true)
                }
            }
            "flags" -> {
                val flag = ItemFlag.values().find { it.name == value.toString() } ?: failedByInvalidValue(instance, key, value)
                instance.itemMeta?.addItemFlags(flag)
            }
            "unbreakable" -> instance.itemMeta?.isUnbreakable = value.applicativeBoolean()
            "itemMeta" -> instance.itemMeta = value as? org.bukkit.inventory.meta.ItemMeta ?: failedByInvalidValue(instance, key, value)
            else -> failedBySetPropertyNotSupported(instance, key)
        }
    }

}