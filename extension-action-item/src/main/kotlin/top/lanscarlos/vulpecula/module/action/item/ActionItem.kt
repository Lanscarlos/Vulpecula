package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.console
import taboolib.platform.util.isNotAir
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
object ActionItem {

    private const val CONTEXT = "@VULPECULA_CONTEXT_ITEM"

    fun getContext(frame: BacikalFrame): ItemStack {
        val item = frame.getVariable<ItemStack>(CONTEXT)
            ?: error(Lang.ACTION_ITEM_EXCEPTION_ITEM_NOT_FOUND.asText(console()))
        require(item.isNotAir()) {
            Lang.ACTION_ITEM_EXCEPTION_ITEM_IS_AIR.asText(console())
        }
        return item
    }

    fun setContext(frame: BacikalFrame, item: ItemStack) {
        frame.setVariable(CONTEXT, item)
    }

}