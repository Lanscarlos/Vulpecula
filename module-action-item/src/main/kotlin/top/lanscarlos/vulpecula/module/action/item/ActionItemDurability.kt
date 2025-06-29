package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import taboolib.module.nms.MinecraftVersion
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
@BacikalParser("item.durability.maximum", aliases = ["max"])
object ActionItemDurabilityMaximum : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        return getMaxDurability(item)
    }

}

@BacikalParser("item.durability.get")
object ActionItemDurabilityGet : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        return getDurability(item)
    }

}

@BacikalParser("item.durability.set")
object ActionItemDurabilitySet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, durability: Int) {
        val item = ActionItem.getContext(frame)
        return setDurability(item, durability)
    }

}

@BacikalParser("item.durability.increase", aliases = ["inc", "add"])
object ActionItemDurabilityIncrease : ClassActionResolver {

    fun resolve(frame: BacikalFrame, durability: Int) {
        val item = ActionItem.getContext(frame)
        return setDurability(item, getDurability(item) + durability)
    }

}

@BacikalParser("item.durability.decrease", aliases = ["dec"])
object ActionItemDurabilityDecrease : ClassActionResolver {

    fun resolve(frame: BacikalFrame, durability: Int) {
        val item = ActionItem.getContext(frame)
        return setDurability(item, getDurability(item) - durability)
    }

}

@BacikalParser("item.durability.repair", aliases = ["fix"])
object ActionItemDurabilityRepair : ClassActionResolver {

    fun resolve(frame: BacikalFrame) {
        val item = ActionItem.getContext(frame)
        return setDurability(item, getMaxDurability(item))
    }

}

/**
 * 物品损耗值
 * */

@Suppress("DEPRECATION")
private fun getDamage(item: ItemStack): Int {
    if (MinecraftVersion.major >= MinecraftVersion.V1_13) {
        val itemMeta = item.itemMeta as? Damageable ?: return 0
        return itemMeta.damage
    } else {
        return item.durability.toInt()
    }
}

@Suppress("DEPRECATION")
private fun setDamage(item: ItemStack, damage: Int) {
    if (MinecraftVersion.major >= MinecraftVersion.V1_13) {
        val itemMeta = item.itemMeta as? Damageable
            ?: error(asLang("module-action-item-exception-durability-unsupported", item.type.name))
        itemMeta.damage = damage
    } else {
        require(damage <= Short.MAX_VALUE) {
            asLang("module-action-item-exception-invalid-damage", damage)
        }
        item.durability = damage.toShort()
    }
}

/**
 * 物品剩余耐久度
 * */
private fun getDurability(item: ItemStack): Int {
    return getMaxDurability(item) - getDamage(item)
}

/**
 * 设置物品剩余耐久度
 * */
private fun setDurability(item: ItemStack, durability: Int) {
    val damage = getMaxDurability(item) - durability
    setDamage(item, damage.coerceIn(0, Short.MAX_VALUE.toInt()))
}

/**
 * 获取物品最大耐久度
 * */
private fun getMaxDurability(item: ItemStack): Int {
    return item.type.maxDurability.toInt()
}