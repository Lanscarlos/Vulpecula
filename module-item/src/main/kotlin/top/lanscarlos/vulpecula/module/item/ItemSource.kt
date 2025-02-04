package top.lanscarlos.vulpecula.module.item

import org.bukkit.inventory.ItemStack

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.item
 *
 * @author Lanscarlos
 * @since 2025-01-13 13:18
 */
interface ItemSource {

    fun get(): ItemStack

    fun set(item: ItemStack)

    fun remove()

    fun toStream(): ItemStream

}