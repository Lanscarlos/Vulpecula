package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemStack
import taboolib.platform.util.isNotAir
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
object ActionItem {

    private const val CONTEXT = "@ITEM"

    fun getContext(frame: BacikalFrame): ItemStack {
        val item = frame.getVariable<ItemStack>(CONTEXT)
            ?: error(asLang("module-action-item-exception-item-not-found"))
        require(item.isNotAir()) {
            asLang("module-action-item-exception-item-is-air")
        }
        return item
    }

    fun setContext(frame: BacikalFrame, item: ItemStack) {
        frame.setVariable(CONTEXT, item)
    }

}