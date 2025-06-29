package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemFlag
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
@BacikalParser("item.flag.size")
object ActionItemFlagSize : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        return item.itemMeta?.itemFlags?.size ?: 0
    }

}

@BacikalParser("item.flag.has")
object ActionItemFlagHas : ClassActionResolver {

    fun resolve(frame: BacikalFrame, flag: String): Boolean {
        val item = ActionItem.getContext(frame)
        val itemFlag = getItemFlag(flag)
        return item.itemMeta?.hasItemFlag(itemFlag) ?: false
    }

}

@BacikalParser("item.flag.add")
object ActionItemFlagAdd : ClassActionResolver {

    fun resolve(frame: BacikalFrame, flag: String) {
        val item = ActionItem.getContext(frame)
        val itemFlag = getItemFlag(flag)
        val itemMeta = item.itemMeta!!
        itemMeta.addItemFlags(itemFlag)
        item.itemMeta = itemMeta
    }

}

@BacikalParser("item.flag.remove")
object ActionItemFlagRemove : ClassActionResolver {

    fun resolve(frame: BacikalFrame, flag: String) {
        val item = ActionItem.getContext(frame)
        val itemFlag = getItemFlag(flag)
        val itemMeta = item.itemMeta!!
        itemMeta.removeItemFlags(itemFlag)
        item.itemMeta = itemMeta
    }

}

private fun getItemFlag(flag: String): ItemFlag {
    return ItemFlag.entries.find { it.name.equals(flag, true) }
        ?: error(asLang("module-action-item-exception-invalid-flag"))
}