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
 * @since 2025/6/30 10:53
 */
@BacikalParser("item.damage.maximum", aliases = ["max"])
object ActionItemDamageMaximum : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        return getMaxDurability(item)
    }

}

@BacikalParser("item.damage.get")
object ActionItemDamageGet : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        return getDamage(item)
    }

}

@BacikalParser("item.damage.set")
object ActionItemDamageSet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, damage: Int) {
        val item = ActionItem.getContext(frame)
        setDamage(item, damage)
    }

}

@BacikalParser("item.damage.increase", aliases = ["inc", "add"])
object ActionItemDamageIncrease : ClassActionResolver {

    fun resolve(frame: BacikalFrame, damage: Int) {
        val item = ActionItem.getContext(frame)
        setDamage(item, getDamage(item) + damage)
    }

}

@BacikalParser("item.damage.increase", aliases = ["dec"])
object ActionItemDamageDecrease : ClassActionResolver {

    fun resolve(frame: BacikalFrame, damage: Int) {
        val item = ActionItem.getContext(frame)
        setDamage(item, getDamage(item) - damage)
    }

}

/**
 * 物品损耗值
 * */
@Suppress("DEPRECATION")
internal fun getDamage(item: ItemStack): Int {
    if (MinecraftVersion.major >= MinecraftVersion.V1_13) {
        val itemMeta = item.itemMeta as? Damageable ?: return 0
        return itemMeta.damage
    } else {
        return item.durability.toInt()
    }
}

/**
 * 物品损耗值
 * */
@Suppress("DEPRECATION")
internal fun setDamage(item: ItemStack, damage: Int) {
    require(damage in 0..getMaxDurability(item)) {
        asLang("module-action-item-exception-invalid-damage", damage)
    }
    if (MinecraftVersion.major >= MinecraftVersion.V1_13) {
        val itemMeta = item.itemMeta as? Damageable
            ?: error(asLang("module-action-item-exception-durability-unsupported", item.type.name))
        itemMeta.damage = damage
    } else {
        item.durability = damage.toShort()
    }
}