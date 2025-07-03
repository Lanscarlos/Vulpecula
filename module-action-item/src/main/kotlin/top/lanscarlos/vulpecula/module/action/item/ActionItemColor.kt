package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/6/30
 */
@BacikalParser("item.color.get")
object ActionItemColorGet : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame,
        @Additional(["alpha"]) alpha: Boolean = false,
        @Additional(["hex"]) hex: Boolean = false
    ): Any? {
        val item = ActionItem.getContext(frame)
        val color =  when (val itemMeta = item.itemMeta) {
            is LeatherArmorMeta -> toStandardColor(itemMeta.color)
            is PotionMeta -> itemMeta.color?.let(::toStandardColor)
            else -> error(asLang("module-action-item-exception-item-unsupported-color", item.type.name))
        }
        return when {
            color == null -> null
            !hex -> color
            !alpha -> "#${color.red.toString(16)}${color.green.toString(16)}${color.blue.toString(16)}"
            else -> "#${color.alpha.toString(16)}${color.red.toString(16)}${color.green.toString(16)}${color.blue.toString(16)}"
        }
    }

}

@BacikalParser("item.color.set")
object ActionItemColorSet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, color: java.awt.Color?) {
        val item = ActionItem.getContext(frame)
        val bukkitColor = color?.let(::toBukkitColor)
        val itemMeta = item.itemMeta
        when (itemMeta) {
            is LeatherArmorMeta -> itemMeta.setColor(bukkitColor)
            is PotionMeta -> itemMeta.color = bukkitColor
            else -> error(asLang("module-action-item-exception-item-unsupported-color", item.type.name))
        }
        item.itemMeta = itemMeta
    }

}

@BacikalParser("item.color.mix")
object ActionItemColorMix : ClassActionResolver {

    fun resolve(frame: BacikalFrame, color: java.awt.Color) {
        val item = ActionItem.getContext(frame)
        val bukkitColor = toBukkitColor(color)
        val itemMeta = item.itemMeta
        when (itemMeta) {
            is LeatherArmorMeta -> itemMeta.setColor(itemMeta.color.mixColors(bukkitColor))
            is PotionMeta -> itemMeta.color = itemMeta.color?.mixColors(bukkitColor) ?: bukkitColor
            else -> error(asLang("module-action-item-exception-item-unsupported-color", item.type.name))
        }
        item.itemMeta = itemMeta
    }

}

private fun toStandardColor(color: org.bukkit.Color): java.awt.Color {
    return try {
        java.awt.Color(color.red, color.green, color.blue, color.alpha)
    } catch (_: NoSuchMethodError) {
        java.awt.Color(color.red, color.green, color.blue)
    }
}

private fun toBukkitColor(color: java.awt.Color): org.bukkit.Color {
    return try {
        org.bukkit.Color.fromARGB(color.alpha, color.red, color.green, color.blue)
    } catch (_: NoSuchMethodError) {
        org.bukkit.Color.fromRGB(color.red, color.green, color.blue)
    }
}