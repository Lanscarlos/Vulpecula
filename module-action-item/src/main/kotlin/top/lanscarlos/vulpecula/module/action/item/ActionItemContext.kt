package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemStack
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
@Parser("item.context")
object ActionItemContext : ClassActionResolver {

    fun resolve(frame: BacikalFrame): ItemStack {
        return ActionItem.getContext(frame)
    }

}