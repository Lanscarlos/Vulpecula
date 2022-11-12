package top.lanscarlos.vulpecula.kether.property.entity

import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.property.GenericProperty
import top.lanscarlos.vulpecula.utils.toFloat

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.entity
 *
 * @author Lanscarlos
 * @since 2022-11-12 15:15
 */

@VulKetherProperty(
    id = "equipment",
    bind = EntityEquipment::class,
    generic = true
)
class EquipmentProperty : GenericProperty<EntityEquipment>("equipment") {

    override fun read(instance: EntityEquipment, key: String): OpenResult {
        val property: Any? = when (key) {
            "armorContents", "armors" -> instance.armorContents

            "hand*" -> instance.itemInHand
            "drop-chance-hand*", "chance-hand*" -> instance.itemInHandDropChance

            "main", "main-hand", "hand" -> instance.itemInMainHand
            "drop-chance-main", "chance-main",
            "drop-chance-main-hand", "chance-main-hand",
            "drop-chance-hand", "chance-hand" -> instance.itemInMainHandDropChance

            "off", "off-hand" -> instance.itemInOffHand
            "drop-chance-off", "chance-off",
            "drop-chance-off-hand", "chance-off-hand" -> instance.itemInOffHandDropChance

            "helmet", "head" -> instance.helmet
            "drop-chance-helmet", "chance-helmet",
            "drop-chance-head", "chance-head" -> instance.helmetDropChance

            "chestplate", "chest" -> instance.chestplate
            "drop-chance-chestplate", "chance-chestplate",
            "drop-chance-chest", "chance-chest" -> instance.chestplateDropChance

            "leggings", "legs", "leg" -> instance.leggings
            "drop-chance-leggings", "chance-leggings",
            "drop-chance-legs", "chance-legs",
            "drop-chance-leg", "chance-leg" -> instance.leggingsDropChance

            "boots", "feet", "foot" -> instance.boots
            "drop-chance-boots", "chance-boots",
            "drop-chance-feet", "chance-feet",
            "drop-chance-foot", "chance-foot" -> instance.bootsDropChance

            "holder" -> instance.holder
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun write(instance: EntityEquipment, key: String, value: Any?): OpenResult {
        when (key) {
            "armorContents", "armors" -> {
                instance.armorContents = value as? Array<out ItemStack> ?: return OpenResult.failed()
            }

            "hand*" -> {
                instance.setItemInHand(value as? ItemStack)
            }
            "drop-chance-hand*", "chance-hand*" -> {
                instance.itemInHandDropChance = value?.toFloat() ?: return OpenResult.failed()
            }

            "main", "main-hand", "hand" -> {
                instance.setItemInMainHand(value as? ItemStack)
            }
            "drop-chance-main", "chance-main",
            "drop-chance-main-hand", "chance-main-hand",
            "drop-chance-hand", "chance-hand" -> {
                instance.itemInMainHandDropChance = value?.toFloat() ?: return OpenResult.failed()
            }

            "off", "off-hand" -> {
                instance.setItemInOffHand(value as? ItemStack)
            }
            "drop-chance-off", "chance-off",
            "drop-chance-off-hand", "chance-off-hand" -> {
                instance.itemInOffHandDropChance = value?.toFloat() ?: return OpenResult.failed()
            }

            "helmet", "head" -> {
                instance.helmet = value as? ItemStack
            }
            "drop-chance-helmet", "chance-helmet",
            "drop-chance-head", "chance-head" -> {
                instance.helmetDropChance = value?.toFloat() ?: return OpenResult.failed()
            }

            "chestplate", "chest" -> {
                instance.chestplate = value as? ItemStack
            }
            "drop-chance-chestplate", "chance-chestplate",
            "drop-chance-chest", "chance-chest" -> {
                instance.chestplateDropChance = value?.toFloat() ?: return OpenResult.failed()
            }

            "leggings", "legs", "leg" -> {
                instance.chestplate = value as? ItemStack
            }
            "drop-chance-leggings", "chance-leggings",
            "drop-chance-legs", "chance-legs",
            "drop-chance-leg", "chance-leg" -> {
                instance.leggingsDropChance = value?.toFloat() ?: return OpenResult.failed()
            }

            "boots", "feet", "foot" -> {
                instance.boots = value as? ItemStack
            }
            "drop-chance-boots", "chance-boots",
            "drop-chance-feet", "chance-feet",
            "drop-chance-foot", "chance-foot" -> {
                instance.bootsDropChance = value?.toFloat() ?: return OpenResult.failed()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}