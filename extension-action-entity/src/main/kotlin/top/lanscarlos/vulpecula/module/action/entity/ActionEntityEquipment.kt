package top.lanscarlos.vulpecula.module.action.entity

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.entity
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
@Parser("entity.equipment.get")
object ActionEntityEquipmentGet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, slot: String): ItemStack {
        val entity = ActionEntity.getContext(frame)
        val equipment = getEquipment(entity)
        val equipmentSlot = getEquipmentSlot(slot)
        return equipment.getItem(equipmentSlot)
    }

}

@Parser("entity.equipment.set")
object ActionEntityEquipmentSet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, slot: String, item: ItemStack) {
        val entity = ActionEntity.getContext(frame)
        val equipment = getEquipment(entity)
        val equipmentSlot = getEquipmentSlot(slot)
        equipment.setItem(equipmentSlot, item)
    }

}

private fun getEquipment(entity: Entity): EntityEquipment {
    return (entity as? LivingEntity)?.equipment
        ?: error(asLang("module-action-entity-exception-equipment-not-found", entity.name))
}

private fun getEquipmentSlot(slot: String): EquipmentSlot {
    return when (slot.uppercase()) {
        "MAIN" -> EquipmentSlot.HAND
        "OFF" -> EquipmentSlot.OFF_HAND
        "HELMET", "HEAD" -> EquipmentSlot.HEAD
        "CHESTPLATE", "CHEST" -> EquipmentSlot.CHEST
        "LEGGINGS", "LEGS" -> EquipmentSlot.LEGS
        "BOOTS", "FEET" -> EquipmentSlot.FEET
        else -> error(asLang("module-action-entity-exception-invalid-slot", slot))
    }
}