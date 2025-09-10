package top.lanscarlos.vulpecula.module.action.item

import top.lanscarlos.vulpecula.common.applicative.IntApplicative
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
@Parser("item.lore.size")
class ActionItemLoreSize : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        return item.itemMeta?.lore?.size ?: 0
    }

}

@Parser("item.lore.get")
class ActionItemLoreGet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, line: String): Any? {
        val item = ActionItem.getContext(frame)
        val lore = item.itemMeta?.lore ?: emptyList<String>()
        if (line == "*" || line == "all") {
            return lore
        }
        val index = IntApplicative.convert(line) - 1
        return lore.getOrNull(index)
    }

}

@Parser("item.lore.insert")
class ActionItemLoreInsert : ClassActionResolver {

    fun resolve(frame: BacikalFrame, line: Int, content: String) {
        val item = ActionItem.getContext(frame)
        val itemMeta = item.itemMeta!!
        val lore = LinkedList(itemMeta.lore ?: mutableListOf<String>())
        val index = line - 1
        require(index in 0..lore.size) {
            val indices = "[1, ${lore.size + 1}]"
            asLang("module-action-item-exception-lore-out-of-bounds", line, indices)
        }
        lore.add(index, content)
        itemMeta.lore = lore
        item.itemMeta = itemMeta
    }

}

@Parser("item.lore.add")
class ActionItemLoreAdd : ClassActionResolver {

    fun resolve(frame: BacikalFrame, content: String) {
        val item = ActionItem.getContext(frame)
        val itemMeta = item.itemMeta!!
        val lore = itemMeta.lore ?: mutableListOf<String>()
        lore.add(content)
        itemMeta.lore = lore
        item.itemMeta = itemMeta
    }

}

@Parser("item.lore.set")
class ActionItemLoreSet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, line: String, content: String) {
        val item = ActionItem.getContext(frame)
        val itemMeta = item.itemMeta!!
        val lore = ArrayList(itemMeta.lore ?: mutableListOf<String>())
        if (line == "*" || line == "all") {
            for (index in lore.indices) {
                lore[index] = content
            }
        } else {
            val index = IntApplicative.convert(line) - 1
            require(index in lore.indices) {
                val indices = "[1, ${lore.size}]"
                asLang("module-action-item-exception-lore-out-of-bounds", line, indices)
            }
            lore[index] = content
        }
        itemMeta.lore = lore
        item.itemMeta = itemMeta
    }

}

@Parser("item.lore.remove")
class ActionItemLoreRemove : ClassActionResolver {

    fun resolve(frame: BacikalFrame, line: String, content: String) {
        val item = ActionItem.getContext(frame)
        val itemMeta = item.itemMeta!!
        val lore = LinkedList(itemMeta.lore ?: mutableListOf<String>())
        if (line == "*" || line == "all") {
            lore.clear()
        } else {
            val index = IntApplicative.convert(line) - 1
            require(index in lore.indices) {
                val indices = "[1, ${lore.size}]"
                asLang("module-action-item-exception-lore-out-of-bounds", line, indices)
            }
            lore.removeAt(index)
        }
        itemMeta.lore = lore
        item.itemMeta = itemMeta
    }

}

@Parser("item.lore.override")
class ActionItemLoreOverride : ClassActionResolver {

    fun resolve(frame: BacikalFrame, lore: List<String>) {
        val item = ActionItem.getContext(frame)
        val itemMeta = item.itemMeta!!
        itemMeta.lore = lore
        item.itemMeta = itemMeta
    }

}

@Parser("item.lore.clear")
class ActionItemLoreClear : ClassActionResolver {

    fun resolve(frame: BacikalFrame) {
        val item = ActionItem.getContext(frame)
        val itemMeta = item.itemMeta!!
        itemMeta.lore = emptyList<String>()
        item.itemMeta = itemMeta
    }

}