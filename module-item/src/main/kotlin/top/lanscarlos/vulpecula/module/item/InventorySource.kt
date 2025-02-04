package top.lanscarlos.vulpecula.module.item

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.item
 *
 * @author Lanscarlos
 * @since 2024-12-14 15:58
 */
class InventorySource(val inventory: Inventory, val slot: Int) : ItemSource {

    override fun get(): ItemStack {
        return inventory.getItem(slot) ?: ItemStack(Material.AIR)
    }

    override fun set(item: ItemStack) {
        inventory.setItem(slot, item)
    }

    override fun remove() {
        set(ItemStack(Material.AIR))
    }

    override fun toStream(): ItemStream {
        return DefaultItemStream(this)
    }

}