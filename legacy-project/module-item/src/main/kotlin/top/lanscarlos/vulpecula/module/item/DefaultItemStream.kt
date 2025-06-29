package top.lanscarlos.vulpecula.module.item

import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import taboolib.library.xseries.XMaterial
import taboolib.module.nms.MinecraftVersion

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.item
 *
 * @author Lanscarlos
 * @since 2025-01-13 12:56
 */
class DefaultItemStream(val source: ItemSource) : ItemStream {

    val item: ItemStack = source.get()

    val itemMeta: ItemMeta = item.itemMeta!!

    override fun apply(): ItemStack {
        item.itemMeta = itemMeta
        source.set(item)
        return item
    }

    override fun destroy() {
        source.remove()
    }

    override fun getMaterial(): XMaterial {
        return XMaterial.matchXMaterial(item.type)
    }

    override fun setMaterial(material: XMaterial) {
        item.type = material.parseMaterial() ?: error("Unknown material: ${material.name}")
    }

    override fun getAmount(): Int {
        return item.amount
    }

    override fun setAmount(amount: Int) {
        item.amount = amount
    }

    @Suppress("DEPRECATION")
    override fun getDamage(): Int {
        return if (MinecraftVersion.major >= 5) {
            // 1.13+
            (itemMeta as Damageable).damage
        } else {
            item.durability.toInt()
        }
    }

    @Suppress("DEPRECATION")
    override fun setDamage(damage: Int) {
        if (MinecraftVersion.major >= 5) {
            // 1.13+
            (itemMeta as Damageable).damage = damage
        } else {
            item.durability = damage.toShort()
        }
    }

    override fun getDurability(): Int {
        return item.type.maxDurability.toInt() - getDamage()
    }

    override fun setDurability(durability: Int) {
        setDamage(item.type.maxDurability.toInt() - durability)
    }

    override fun getMaxDurability(): Int {
        return item.type.maxDurability.toInt()
    }

    override fun getDisplayName(): String {
        return itemMeta.displayName
    }

    override fun setDisplayName(name: String?) {
        itemMeta.setDisplayName(name)
    }

    override fun getLore(): List<String> {
        return itemMeta.lore ?: emptyList()
    }

    override fun setLore(lore: List<String>) {
        itemMeta.lore = lore
    }

    override fun clearLore() {
        itemMeta.lore = null
    }

    override fun appendLore(lore: List<String>) {
        val current = getLore().toMutableList()
        current.addAll(lore)
        setLore(current)
    }

    override fun getFlags(): Set<ItemFlag> {
        return itemMeta.itemFlags
    }

    override fun setFlags(flags: Set<ItemFlag>) {
        itemMeta.addItemFlags(*flags.toTypedArray())
    }

    override fun removeFlags(vararg flag: ItemFlag) {
        itemMeta.removeItemFlags(*flag)
    }

    override fun getEnchantments(): Map<Enchantment, Int> {
        return itemMeta.enchants
    }

    override fun setEnchantments(enchantments: Map<Enchantment, Int>) {
        // 移除原有附魔
        clearEnchantments()

        // 添加新附魔
        appendEnchantments(enchantments)
    }

    override fun clearEnchantments() {
        for (enchantment in getEnchantments().keys) {
            itemMeta.removeEnchant(enchantment)
        }
    }

    override fun appendEnchantments(enchantments: Map<Enchantment, Int>) {
        for ((key, value) in enchantments) {
            itemMeta.addEnchant(key, value, true)
        }
    }

    override fun hasEnchantment(enchantment: Enchantment): Boolean {
        return itemMeta.hasEnchant(enchantment)
    }

    override fun hasConflictingEnchant(enchantment: Enchantment): Boolean {
        return itemMeta.hasConflictingEnchant(enchantment)
    }

    override fun getEnchantmentLevel(enchantment: Enchantment): Int {
        return itemMeta.getEnchantLevel(enchantment)
    }

    override fun setEnchantmentLevel(enchantment: Enchantment, level: Int) {
        itemMeta.addEnchant(enchantment, level, true)
    }

    override fun setEnchantmentLevel(enchantment: Enchantment, level: Int, ignoreLevelRestriction: Boolean) {
        itemMeta.addEnchant(enchantment, level, ignoreLevelRestriction)
    }

    override fun removeEnchantment(enchantment: Enchantment) {
        itemMeta.removeEnchant(enchantment)
    }

    override fun getColor(): Color? {
        return when (itemMeta) {
            is LeatherArmorMeta -> itemMeta.color
            is PotionMeta -> itemMeta.color
            else -> error("ItemMeta is not supported color meta.")
        }
    }

    override fun setColor(color: Color?) {
        when (itemMeta) {
            is LeatherArmorMeta -> itemMeta.setColor(color)
            is PotionMeta -> itemMeta.color = color
            else -> error("ItemMeta is not supported color meta.")
        }
    }

    override fun mixColor(vararg color: Color) {
        getColor()?.mixColors(*color)?.let { setColor(it) } ?: error("ItemMeta is not supported color meta.")
    }

    override fun mixDyeColor(vararg color: DyeColor) {
        getColor()?.mixDyes(*color)?.let { setColor(it) } ?: error("ItemMeta is not supported color meta.")
    }

}