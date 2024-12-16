package top.lanscarlos.vulpecula.item

import org.bukkit.Registry
import org.bukkit.enchantments.Enchantment

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.item
 *
 * @author Lanscarlos
 * @since 2024-12-15 14:28
 */
class ItemStream(val source: ItemSource) {

    val item = source.get()

    var itemMeta = item.itemMeta!!

    fun hasDisplayName(): Boolean {
        return itemMeta.hasDisplayName()
    }

    fun getDisplayName(): String? {
        return if (hasDisplayName()) itemMeta.displayName else null
    }

    fun setDisplayName(name: String?) {
        itemMeta.setDisplayName(name)
    }

    fun hasLore(): Boolean {
        return itemMeta.hasLore()
    }

    fun appendLore(lore: String) {
        appendLore(listOf(lore))
    }

    fun appendLore(lore: List<String>) {
        val loreList = getLore().toMutableList()
        loreList.addAll(lore)
        setLore(loreList)
    }

    fun getLore(): List<String> {
        return itemMeta.lore ?: emptyList()
    }

    fun setLore(lore: List<String>) {
        itemMeta.lore = lore
    }

    fun hasEnchantments(): Boolean {
        return itemMeta.hasEnchants()
    }

    fun hasEnchantment(enchantment: String): Boolean {
        return itemMeta.hasEnchant(enchantment.toEnchantment())
    }

    fun addEnchantment(enchantment: String, level: Int) {
        itemMeta.addEnchant(enchantment.toEnchantment(), level, false)
    }

    fun addEnchantment(enchantment: String, level: Int, ignoreLevelRestriction: Boolean) {
        itemMeta.addEnchant(enchantment.toEnchantment(), level, ignoreLevelRestriction)
    }

    fun apply() {
        item.itemMeta = itemMeta
        source.set(item)
    }

    fun String.toEnchantment(): Enchantment {
        for (enchantment in Registry.ENCHANTMENT.iterator()) {
            if (enchantment.key.key.equals(this, true)) {
                return enchantment
            }
        }
        error("Unknown enchantment: $this")
    }

}