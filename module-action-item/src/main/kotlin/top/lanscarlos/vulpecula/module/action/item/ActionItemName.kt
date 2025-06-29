package top.lanscarlos.vulpecula.module.action.item

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
@BacikalParser("item.name.get")
object ActionItemNameGet : ClassActionResolver {

    fun resolve(frame: BacikalFrame): String? {
        val item = ActionItem.getItem(frame)
        return item.itemMeta?.displayName
    }

}

@BacikalParser("item.name.set")
object ActionItemNameSet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, name: String?) {
        val item = ActionItem.getItem(frame)
        item.itemMeta = item.itemMeta!!.also {
            it.setDisplayName(name)
        }
    }

}