package top.lanscarlos.vulpecula.module.item

import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import taboolib.type.BukkitEquipment

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.item
 *
 * @author Lanscarlos
 * @since 2024-12-14 16:05
 */
class EquipmentSource(val entity: LivingEntity, val slot: BukkitEquipment) : ItemSource {

    override fun get(): ItemStack {
        return slot.getItem(entity) ?: ItemStack(Material.AIR)
    }

    override fun set(item: ItemStack) {
        slot.setItem(entity, item)
    }

    override fun remove() {
        set(ItemStack(Material.AIR))
    }

}