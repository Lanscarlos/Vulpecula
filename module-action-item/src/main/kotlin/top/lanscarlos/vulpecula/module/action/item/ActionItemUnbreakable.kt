package top.lanscarlos.vulpecula.module.action.item

import taboolib.common.platform.function.warning
import taboolib.library.reflex.Reflex.Companion.invokeMethod
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
@BacikalParser("item.unbreakable.state")
object ActionItemUnbreakableState : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Boolean {
        val item = ActionItem.getContext(frame)
        val itemMeta = item.itemMeta ?: return false
        return try {
            itemMeta.isUnbreakable
        } catch (_: NoSuchMethodError) {
            try {
                itemMeta.invokeMethod<Any>("spigot")!!
                    .invokeMethod<Boolean>("isUnbreakable") == true
            } catch (_: NoSuchMethodException) {
                warning(asLang("module-action-item-exception-unbreakable-unsupported"))
                false
            }
        }
    }

}

@BacikalParser("item.unbreakable.enable")
object ActionItemUnbreakableEnable : ClassActionResolver {

    fun resolve(frame: BacikalFrame) {
        val item = ActionItem.getContext(frame)
        val itemMeta = item.itemMeta!!
        try {
            itemMeta.isUnbreakable = true
        } catch (_: NoSuchMethodError) {
            try {
                itemMeta.invokeMethod<Any>("spigot")!!
                    .invokeMethod<Boolean>("setUnbreakable", true)
            } catch (_: NoSuchMethodException) {
                warning(asLang("module-action-item-exception-unbreakable-unsupported"))
                false
            }
        }
        item.itemMeta = itemMeta
    }

}

@BacikalParser("item.unbreakable.disable")
object ActionItemUnbreakableDisable : ClassActionResolver {

    fun resolve(frame: BacikalFrame) {
        val item = ActionItem.getContext(frame)
        val itemMeta = item.itemMeta!!
        try {
            itemMeta.isUnbreakable = false
        } catch (_: NoSuchMethodError) {
            try {
                itemMeta.invokeMethod<Any>("spigot")!!
                    .invokeMethod<Boolean>("setUnbreakable", false)
            } catch (_: NoSuchMethodException) {
                warning(asLang("module-action-item-exception-unbreakable-unsupported"))
                false
            }
        }
        item.itemMeta = itemMeta
    }

}