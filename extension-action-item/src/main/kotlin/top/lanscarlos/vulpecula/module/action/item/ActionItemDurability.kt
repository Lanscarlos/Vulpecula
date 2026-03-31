package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.console
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
@Parser("item.durability.maximum", aliases = ["max"])
object ActionItemDurabilityMaximum : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        return getMaxDurability(item)
    }

}

@Parser("item.durability.get")
object ActionItemDurabilityGet : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        return getDurability(item)
    }

}

@Parser("item.durability.set")
object ActionItemDurabilitySet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, durability: Int) {
        val item = ActionItem.getContext(frame)
        return setDurability(item, durability)
    }

}

@Parser("item.durability.increase", aliases = ["inc", "add"])
object ActionItemDurabilityIncrease : ClassActionResolver {

    fun resolve(frame: BacikalFrame, durability: Int) {
        val item = ActionItem.getContext(frame)
        return setDurability(item, getDurability(item) + durability)
    }

}

@Parser("item.durability.decrease", aliases = ["dec"])
object ActionItemDurabilityDecrease : ClassActionResolver {

    fun resolve(frame: BacikalFrame, durability: Int) {
        val item = ActionItem.getContext(frame)
        return setDurability(item, getDurability(item) - durability)
    }

}

@Parser("item.durability.repair", aliases = ["fix"])
object ActionItemDurabilityRepair : ClassActionResolver {

    fun resolve(frame: BacikalFrame) {
        val item = ActionItem.getContext(frame)
        return setDurability(item, getMaxDurability(item))
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
    require(durability in 0..getMaxDurability(item)) {
        Lang.ACTION_ITEM_EXCEPTION_INVALID_DURABILITY.asText(console(), durability)
    }
    setDamage(item, getMaxDurability(item) - durability)
}

/**
 * 获取物品最大耐久度
 * */
internal fun getMaxDurability(item: ItemStack): Int {
    return item.type.maxDurability.toInt()
}