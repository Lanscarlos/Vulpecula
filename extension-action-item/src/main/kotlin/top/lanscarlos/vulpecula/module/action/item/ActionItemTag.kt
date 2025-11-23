package top.lanscarlos.vulpecula.module.action.item

import taboolib.module.nms.getItemTag
import taboolib.module.nms.setItemTag
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Expected
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.annotation.Optional
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/7/2
 */
@Parser("item.tag.has")
object ActionItemTagHas : ClassActionResolver {

    fun resolve(frame: BacikalFrame, key: String): Boolean {
        val item = ActionItem.getContext(frame)
        return item.getItemTag().getDeep(key) != null
    }

}

@Parser("item.tag.get")
object ActionItemTagGet : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame,
        key: String,
        @Optional(["as"]) type: String = "~"
    ): Any? {
        val item = ActionItem.getContext(frame)
        val tag = item.getItemTag()
        val data = if (key == "*") tag else tag.getDeep(key) ?: return null
        return when (type.lowercase()) {
            "~" -> data.unsafeData()
            "boolean", "bool" -> data.asByte() > 0
            "short" -> data.asShort()
            "integer", "int" -> data.asInt()
            "long" -> data.asLong()
            "float" -> data.asFloat()
            "double" -> data.asDouble()
            "string", "str" -> data.asString()
            else -> error(asLang("module-action-item-exception-invalid-tag-type", type))
        }
    }

}

@Parser("item.tag.set")
object ActionItemTagSet : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame,
        key: String,
        @Expected(["to"]) value: Any
    ) {
        val item = ActionItem.getContext(frame)
        val tag = item.getItemTag()
        tag.putDeep(key, value)
        val newItem = item.setItemTag(tag)
        // 将 nbt 更新后的新物品 meta 转入原物品
        item.also { it.itemMeta = newItem.itemMeta }
    }

}

@Parser("item.tag.remove")
object ActionItemTagRemove : ClassActionResolver {

    fun resolve(frame: BacikalFrame, key: String) {
        val item = ActionItem.getContext(frame)
        val tag = item.getItemTag()
        tag.removeDeep(key)
        val newItem = item.setItemTag(tag)
        // 将 nbt 更新后的新物品 meta 转入原物品
        item.also { it.itemMeta = newItem.itemMeta }
    }

}