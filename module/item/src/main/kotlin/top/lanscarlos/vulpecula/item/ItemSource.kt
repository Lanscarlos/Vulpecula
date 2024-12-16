package top.lanscarlos.vulpecula.item

import org.bukkit.inventory.ItemStack

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.item
 *
 * 物品源
 *
 * @author Lanscarlos
 * @since 2024-12-14 15:58
 */
interface ItemSource {

    fun get(): ItemStack

    fun set(item: ItemStack)

    fun remove()

}