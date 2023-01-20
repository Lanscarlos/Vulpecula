package top.lanscarlos.vulpecula.utils

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import taboolib.module.nms.MinecraftVersion

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2023-01-20 12:12
 */

/**
 * 物品最大耐久度
 * */
val ItemStack.maxDurability: Int
    get() = this.type.maxDurability.toInt()

/**
 * 物品损耗度
 * */
@Suppress("deprecation")
var ItemStack.damage: Int
    get() {
        // 1.13+
        return if (MinecraftVersion.major >= 5) {
            (this.itemMeta as? Damageable)?.damage ?: 0
        } else {
            this.durability.toInt()
        }
    }
    set(value) {
        // 1.13+
        if (MinecraftVersion.major >= 5) {
            (this.itemMeta as? Damageable)?.damage = value
        } else {
            this.durability = value.toShort()
        }
    }

/**
 * 物品耐久度
 * */
var ItemStack.duraFix: Int
    get() = this.maxDurability - damage
    set(value) {
        this.damage = this.maxDurability - value
    }