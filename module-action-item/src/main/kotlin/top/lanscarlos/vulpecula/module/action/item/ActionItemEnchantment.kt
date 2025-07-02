package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.Registry
import org.bukkit.enchantments.Enchantment
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
@BacikalParser("item.enchantment.size")
object ActionItemEnchantmentSize : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        return item.itemMeta?.enchants?.size ?: 0
    }

}

@BacikalParser("item.enchantment.has")
object ActionItemEnchantmentHas : ClassActionResolver {

    fun resolve(frame: BacikalFrame, enchantment: String): Boolean {
        val item = ActionItem.getContext(frame)
        val bukkitEnchantment = getEnchantment(enchantment)
        return item.itemMeta?.hasEnchant(bukkitEnchantment) ?: false
    }

}

@BacikalParser("item.enchantment.get")
object ActionItemEnchantmentGet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, enchantment: String): Int {
        val item = ActionItem.getContext(frame)
        val bukkitEnchantment = getEnchantment(enchantment)
        return item.itemMeta?.getEnchantLevel(bukkitEnchantment) ?: 0
    }

}

@BacikalParser("item.enchantment.set", aliases = ["add"])
object ActionItemEnchantmentSet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, enchantment: String, level: Int): Boolean {
        val item = ActionItem.getContext(frame)
        val bukkitEnchantment = getEnchantment(enchantment)
        val itemMeta = item.itemMeta!!
        require(level >= 0) {
            asLang("module-action-item-exception-invalid-enchantment-level", level)
        }
        val result = itemMeta.addEnchant(bukkitEnchantment, level, true)
        item.itemMeta = itemMeta
        return result
    }

}

@BacikalParser("item.enchantment.remove")
object ActionItemEnchantmentRemove : ClassActionResolver {

    fun resolve(frame: BacikalFrame, enchantment: String): Boolean {
        val item = ActionItem.getContext(frame)
        val bukkitEnchantment = getEnchantment(enchantment)
        val itemMeta = item.itemMeta!!
        val result = itemMeta.removeEnchant(bukkitEnchantment)
        item.itemMeta = itemMeta
        return result
    }

}

@BacikalParser("item.enchantment.clear")
object ActionItemEnchantmentClear : ClassActionResolver {

    fun resolve(frame: BacikalFrame) {
        val item = ActionItem.getContext(frame)
        val itemMeta = item.itemMeta!!
        for (enchantment in itemMeta.enchants.keys) {
            itemMeta.removeEnchant(enchantment)
        }
        item.itemMeta = itemMeta
    }

}

private fun getEnchantment(enchantment: String): Enchantment {
    return enchantmentMap[enchantment.uppercase()] ?: error(asLang("module-action-item-exception-invalid-enchantment-type", enchantment))
}

private val enchantmentMap: Map<String, Enchantment> = mapOf(
    "PROTECTION_ENVIRONMENTAL" to Enchantment.PROTECTION_ENVIRONMENTAL,
    "PROTECTION_FIRE" to Enchantment.PROTECTION_FIRE,
    "PROTECTION_FALL" to Enchantment.PROTECTION_FALL,
    "PROTECTION_EXPLOSIONS" to Enchantment.PROTECTION_EXPLOSIONS,
    "PROTECTION_PROJECTILE" to Enchantment.PROTECTION_PROJECTILE,
    "OXYGEN" to Enchantment.OXYGEN,
    "WATER_WORKER" to Enchantment.WATER_WORKER,
    "THORNS" to Enchantment.THORNS,
    "DEPTH_STRIDER" to Enchantment.DEPTH_STRIDER,
    "FROST_WALKER" to Enchantment.FROST_WALKER,
    "BINDING_CURSE" to Enchantment.BINDING_CURSE,
    "DAMAGE_ALL" to Enchantment.DAMAGE_ALL,
    "DAMAGE_UNDEAD" to Enchantment.DAMAGE_UNDEAD,
    "DAMAGE_ARTHROPODS" to Enchantment.DAMAGE_ARTHROPODS,
    "KNOCKBACK" to Enchantment.KNOCKBACK,
    "FIRE_ASPECT" to Enchantment.FIRE_ASPECT,
    "LOOT_BONUS_MOBS" to Enchantment.LOOT_BONUS_MOBS,
    "SWEEPING_EDGE" to Enchantment.SWEEPING_EDGE,
    "DIG_SPEED" to Enchantment.DIG_SPEED,
    "SILK_TOUCH" to Enchantment.SILK_TOUCH,
    "DURABILITY" to Enchantment.DURABILITY,
    "LOOT_BONUS_BLOCKS" to Enchantment.LOOT_BONUS_BLOCKS,
    "ARROW_DAMAGE" to Enchantment.ARROW_DAMAGE,
    "ARROW_KNOCKBACK" to Enchantment.ARROW_KNOCKBACK,
    "ARROW_FIRE" to Enchantment.ARROW_FIRE,
    "ARROW_INFINITE" to Enchantment.ARROW_INFINITE,
    "LUCK" to Enchantment.LUCK,
    "LURE" to Enchantment.LURE,
    "LOYALTY" to Enchantment.LOYALTY,
    "IMPALING" to Enchantment.IMPALING,
    "RIPTIDE" to Enchantment.RIPTIDE,
    "CHANNELING" to Enchantment.CHANNELING,
    "MULTISHOT" to Enchantment.MULTISHOT,
    "QUICK_CHARGE" to Enchantment.QUICK_CHARGE,
    "PIERCING" to Enchantment.PIERCING,
    "MENDING" to Enchantment.MENDING,
    "VANISHING_CURSE" to Enchantment.VANISHING_CURSE,
    "SOUL_SPEED" to Enchantment.SOUL_SPEED,
    "SWIFT_SNEAK" to Enchantment.SWIFT_SNEAK
)