package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.enchantments.Enchantment
import taboolib.common.platform.function.console
import taboolib.library.xseries.XEnchantment
import taboolib.module.nms.MinecraftVersion
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver
import kotlin.jvm.optionals.getOrNull

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
@Parser("item.enchantment.size")
object ActionItemEnchantmentSize : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        return item.itemMeta?.enchants?.size ?: 0
    }

}

@Parser("item.enchantment.has")
object ActionItemEnchantmentHas : ClassActionResolver {

    fun resolve(frame: BacikalFrame, enchantment: String): Boolean {
        val item = ActionItem.getContext(frame)
        val bukkitEnchantment = getEnchantment(enchantment)
        return item.itemMeta?.hasEnchant(bukkitEnchantment) ?: false
    }

}

@Parser("item.enchantment.get")
object ActionItemEnchantmentGet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, enchantment: String): Int {
        val item = ActionItem.getContext(frame)
        val bukkitEnchantment = getEnchantment(enchantment)
        return item.itemMeta?.getEnchantLevel(bukkitEnchantment) ?: 0
    }

}

@Parser("item.enchantment.set", aliases = ["add"])
object ActionItemEnchantmentSet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, enchantment: String, level: Int): Boolean {
        val item = ActionItem.getContext(frame)
        val bukkitEnchantment = getEnchantment(enchantment)
        val itemMeta = item.itemMeta!!
        require(level >= 0) {
            Lang.ACTION_ITEM_EXCEPTION_INVALID_ENCHANTMENT_LEVEL.asText(console(), level)
        }
        val result = itemMeta.addEnchant(bukkitEnchantment, level, true)
        item.itemMeta = itemMeta
        return result
    }

}

@Parser("item.enchantment.remove")
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

@Parser("item.enchantment.clear")
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
    val xEnchantment = XEnchantment.of(enchantment.uppercase()).getOrNull()
        ?: error(Lang.ACTION_ITEM_EXCEPTION_INVALID_ENCHANTMENT_TYPE.asText(console(), enchantment))
    return xEnchantment.get()
        ?: error(Lang.ACTION_ITEM_EXCEPTION_UNSUPPORTED_ENCHANTMENT_TYPE.asText(console(), MinecraftVersion.runningVersion, enchantment))
}