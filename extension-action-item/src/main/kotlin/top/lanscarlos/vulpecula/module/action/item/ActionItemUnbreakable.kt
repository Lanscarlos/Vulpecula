package top.lanscarlos.vulpecula.module.action.item

import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion
import top.lanscarlos.vulpecula.common.core.utils.asLang
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
@Parser("item.unbreakable.state")
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
                error(asLang("module-action-item-exception-unsupported-unbreakable", MinecraftVersion.runningVersion))
            }
        }
    }

}

@Parser("item.unbreakable.enable")
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
                error(asLang("module-action-item-exception-unsupported-unbreakable", MinecraftVersion.runningVersion))
            }
        }
        item.itemMeta = itemMeta
    }

}

@Parser("item.unbreakable.disable")
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
                error(asLang("module-action-item-exception-unsupported-unbreakable", MinecraftVersion.runningVersion))
            }
        }
        item.itemMeta = itemMeta
    }

}