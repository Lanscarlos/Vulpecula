package top.lanscarlos.vulpecula.module.action.item

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
@Parser("item.name.get")
object ActionItemNameGet : ClassActionResolver {

    fun resolve(frame: BacikalFrame): String? {
        val item = ActionItem.getContext(frame)
        return item.itemMeta?.displayName
    }

}

@Parser("item.name.set")
object ActionItemNameSet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, name: String?) {
        val item = ActionItem.getContext(frame)
        item.itemMeta = item.itemMeta!!.also {
            it.setDisplayName(name)
        }
    }

}