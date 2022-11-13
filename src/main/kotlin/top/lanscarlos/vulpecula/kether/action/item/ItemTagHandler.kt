package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.inventory.ItemStack
import taboolib.library.kether.QuestReader
import taboolib.module.kether.run
import taboolib.module.nms.getItemTag
import taboolib.module.nms.setItemTag
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.readItemStack
import top.lanscarlos.vulpecula.utils.tryNextAction
import top.lanscarlos.vulpecula.utils.tryReadString

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 15:01
 */
object ItemTagHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("nbt", "tag")

    override fun read(isRoot: Boolean, reader: QuestReader): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        reader.mark()
        return when (reader.nextToken()) {
            "get" -> getTag(reader, source)
            "set" -> setTag(reader, source)
            "has" -> hasTag(reader, source)
            "all" -> allTag(source)
            else -> {
                reader.reset()
                getTag(reader, source)
            }
        }
    }

    private fun getTag(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val path = StringLiveData(reader.nextBlock())
        val asType = reader.tryReadString("as")
        val defAction = reader.tryNextAction("def")
        return acceptHandler(source) { item ->
            val tag = item.getItemTag()
            val def = defAction?.let { this.run(it).join() }
            val key = path.getOrNull(this) ?: return@acceptHandler def
            val data = tag.getDeep(key) ?: return@acceptHandler def
            return@acceptHandler when (asType?.getOrNull(this)) {
                "boolean", "bool" -> data.asByte() > 0
                "short" -> data.asShort()
                "integer", "int" -> data.asInt()
                "long" -> data.asLong()
                "float" -> data.asFloat()
                "double" -> data.asDouble()
                "string", "str" -> data.asString()
                else -> data.unsafeData() ?: def
            }
        }
    }

    private fun setTag(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val path = StringLiveData(reader.nextBlock())
        val value = reader.nextBlock()
        return applyTransfer(source) { item, _ ->
            val tag = item.getItemTag()
            val key = path.getOrNull(this) ?: error("No path selected.")
            tag.putDeep(key, this.run(value).join())
            val newItem = item.setItemTag(tag)

            // 将 nbt 更新后的新物品 meta 转入原物品
            return@applyTransfer newItem.itemMeta
        }
    }

    private fun hasTag(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val path = StringLiveData(reader.nextBlock())
        return acceptHandler(source) { item ->
            val key = path.getOrNull(this) ?: return@acceptHandler false
            return@acceptHandler item.getItemTag().getDeep(key) != null
        }
    }

    private fun allTag(source: LiveData<ItemStack>?): ActionItemStack.Handler {
        return acceptHandler(source) { item -> item.getItemTag() }
    }
}