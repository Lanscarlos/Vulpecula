package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import taboolib.library.xseries.XPotion
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
 * @since 2025/7/2
 */
@BacikalParser("item.potion.size")
object ActionItemPotionSize : ClassActionResolver {

    @Suppress("DEPRECATION")
    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        val itemMeta = getPotionMeta(item)
        return try {
            itemMeta.customEffects.size + 1
        } catch (_: Exception) {
            1
        }
    }

}

@BacikalParser("item.potion.has")
object ActionItemPotionHas : ClassActionResolver {

    @Suppress("DEPRECATION")
    fun resolve(frame: BacikalFrame, type: String): Boolean {
        val item = ActionItem.getContext(frame)
        val itemMeta = getPotionMeta(item)
        val xPotion = getXPotion(type)

        // 判断药水自定义类型
        val potionEffectType = xPotion.potionEffectType!!
        if (itemMeta.hasCustomEffect(potionEffectType)) {
            return true
        }

        // 判断药水主类型
        val potionType = xPotion.potionType
        return if (MinecraftVersion.versionId >= 12002) {
            // v1.20.2+
            potionType == itemMeta.basePotionType
        } else {
            potionType == itemMeta.basePotionData.type
        }
    }

}

private fun getPotionMeta(item: ItemStack): PotionMeta {
    return item.itemMeta as? PotionMeta ?: error(asLang("module-action-item-exception-potion-unsupported", item.type.name))
}

private fun getXPotion(type: String): XPotion {
    val xPotion = XPotion.entries.find { it.name.equals(type, true) }
        ?: error(asLang("module-action-item-exception-invalid-potion-type", type))
    require(xPotion.isSupported) {
        asLang("module-action-item-exception-unsupported-potion-type", MinecraftVersion.runningVersion, type)
    }
    return xPotion
}