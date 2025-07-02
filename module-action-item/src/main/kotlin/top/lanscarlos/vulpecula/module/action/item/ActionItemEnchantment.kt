package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.enchantments.Enchantment
import taboolib.library.xseries.XEnchantment
import taboolib.module.nms.MinecraftVersion
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
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
    val xEnchantment = XEnchantment.of(enchantment.uppercase()).getOrNull()
        ?: error(asLang("module-action-item-exception-invalid-enchantment-type", enchantment))
    return xEnchantment.get()
        ?: error(asLang("module-action-item-exception-unsupported-enchantment-type", MinecraftVersion.runningVersion, enchantment))
}