package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemStack
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
@BacikalParser("item.switch")
object ActionItemSwitch : ClassActionResolver {

    fun resolve(frame: BacikalFrame, item: ItemStack) {
        ActionItem.setContext(frame, item)
    }

}