package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.library.xseries.XPotion
import taboolib.module.nms.MinecraftVersion
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/7/2
 */
@Parser("item.potion.size")
object ActionItemPotionSize : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        val itemMeta = getPotionMeta(item)
        if (!itemMeta.hasCustomEffects()) {
            return 0
        }
        return itemMeta.customEffects.size
    }

}

@Parser("item.potion.has")
object ActionItemPotionHas : ClassActionResolver {

    fun resolve(frame: BacikalFrame, type: String): Boolean {
        val item = ActionItem.getContext(frame)
        val itemMeta = getPotionMeta(item)
        val potionEffectType = getPotionEffectType(type)
        return itemMeta.hasCustomEffect(potionEffectType)
    }

}

@Parser("item.potion.set", aliases = ["add"])
object ActionItemPotionSet : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame,
        type: String,
        duration: Int,
        level: Int,
        @Additional(["ambient", "amb", "a"]) ambient: Boolean = false,
        @Additional(["particles", "particle", "p"]) particles: Boolean = true,
        @Additional(["icon", "i"]) icon: Boolean = true
    ): Boolean {
        require(duration > 0) { "duration must be greater than 0" }
        require(level > 0) { "level must be greater than 0" }
        val item = ActionItem.getContext(frame)
        val itemMeta = getPotionMeta(item)
        val potionEffectType = getPotionEffectType(type)
        val amplifier = level - 1
        val potionEffect = try {
            PotionEffect(potionEffectType, duration, amplifier, ambient, particles, icon)
        } catch (_: NoSuchMethodError) {
            // 兼容 v1.12.2
            PotionEffect(potionEffectType, duration, amplifier, ambient, particles)
        }
        val exist = itemMeta.hasCustomEffect(potionEffectType)
        itemMeta.addCustomEffect(potionEffect, true)
        item.itemMeta = itemMeta
        return exist
    }

}

@Parser("item.potion.remove")
object ActionItemPotionRemove : ClassActionResolver {

    fun resolve(frame: BacikalFrame, type: String): Boolean {
        val item = ActionItem.getContext(frame)
        val itemMeta = getPotionMeta(item)
        val potionEffectType = getPotionEffectType(type)
        if (!itemMeta.hasCustomEffect(potionEffectType)) {
            return false
        }
        itemMeta.removeCustomEffect(potionEffectType)
        item.itemMeta = itemMeta
        return true
    }

}

@Parser("item.potion.clear")
object ActionItemPotionClear : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Boolean {
        val item = ActionItem.getContext(frame)
        val itemMeta = getPotionMeta(item)
        if (!itemMeta.hasCustomEffects()) {
            return false
        }
        for (effect in itemMeta.customEffects) {
            itemMeta.removeCustomEffect(effect.type)
        }
        item.itemMeta = itemMeta
        return true
    }

}

private fun getPotionMeta(item: ItemStack): PotionMeta {
    return item.itemMeta as? PotionMeta ?: error(asLang("module-action-item-exception-item-unsupported-potion", item.type.name))
}

private fun getPotionEffectType(type: String): PotionEffectType {
    return getXPotion(type).potionEffectType!!
}

private fun getXPotion(type: String): XPotion {
    val xPotion = XPotion.entries.find { it.name.equals(type, true) }
        ?: error(asLang("module-action-item-exception-invalid-potion-type", type))
    require(xPotion.isSupported) {
        asLang("module-action-item-exception-unsupported-potion-type", MinecraftVersion.runningVersion, type)
    }
    return xPotion
}