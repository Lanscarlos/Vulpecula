package top.lanscarlos.vulpecula.module.item

import org.bukkit.inventory.ItemStack

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.item
 *
 * @author Lanscarlos
 * @since 2024-11-23 14:50
 */
class ItemHolder(val item: ItemStack) {

    var itemMeta = item.itemMeta!!

    var name: String? = itemMeta.displayName

    var lore = itemMeta.lore ?: mutableListOf()

    fun name(name: String) {
        this.name = name
    }

    fun appendName(name: String) {
        this.name = this.name?.plus(name) ?: name
    }

    /**
     * 追加一行 Lore
     */
    fun appendLore(line: String) {
        lore.add(line)
    }

    fun insertLore(line: String, index: Int) {
        lore.add(index, line)
    }

    fun insertLoreBefore(line: String, pattern: String) {
        val regex = pattern.toRegex()
        for ((i, it) in lore.withIndex()) {
            if (it.matches(regex)) {
                lore.add(i, line)
                break
            }
        }
    }

    fun insertLoreAfter(line: String, pattern: String) {
        val regex = pattern.toRegex()
        for ((i, it) in lore.withIndex()) {
            if (it.matches(regex)) {
                lore.add(i + 1, line)
                break
            }
        }
    }

    fun modifyLore(index: Int, line: String) {
        lore[index] = line
    }

    fun removeLore(index: Int) {
        lore.removeAt(index)
    }

    fun removeLore(pattern: String) {
        val regex = pattern.toRegex()
        lore.removeIf { it.matches(regex) }
    }

    fun clearLore() {
        lore.clear()
    }

    fun apply() {
        itemMeta.setDisplayName(name)
        itemMeta.lore = lore
        item.itemMeta = itemMeta
    }

}