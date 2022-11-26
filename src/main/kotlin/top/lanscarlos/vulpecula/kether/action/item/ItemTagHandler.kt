package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.inventory.ItemStack
import taboolib.library.kether.QuestReader
import taboolib.module.kether.run
import taboolib.module.nms.getItemTag
import taboolib.module.nms.setItemTag
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 15:01
 */
object ItemTagHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("nbt", "tag")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
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
        val path = reader.readString()
        val asType = reader.tryReadString("as")
        val defAction = reader.tryNextAction("def")
        return acceptHandleFuture(source) { item ->
            listOf(
                path.getOrNull(this),
                asType?.getOrNull(this),
                defAction?.let { this.run(it) }
            ).thenTake().thenApply { args ->
                val def = args[2]
                val key = args[0]?.toString() ?: return@thenApply def
                val data = item.getItemTag().getDeep(key) ?: return@thenApply def

                return@thenApply when (args[1]?.toString()) {
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
    }

    private fun setTag(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val path = reader.readString()
        val value = reader.nextBlock()

        return acceptTransferFuture(source) { item ->
            listOf(
                path.getOrNull(this),
                this.run(value)
            ).thenTake().thenApply { args ->
                val tag = item.getItemTag()
                val key = args[0]?.toString() ?: error("No path selected.")
                tag.putDeep(key, args[1])
                val newItem = item.setItemTag(tag)

                // 将 nbt 更新后的新物品 meta 转入原物品
                return@thenApply item.also { it.itemMeta = newItem.itemMeta }
            }
        }
    }

    private fun hasTag(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val path = StringLiveData(reader.nextBlock())
        return acceptHandleFuture(source) { item ->
            path.getOrNull(this).thenApply { key ->
                if (key == null) return@thenApply false
                return@thenApply item.getItemTag().getDeep(key) != null
            }
        }
    }

    private fun allTag(source: LiveData<ItemStack>?): ActionItemStack.Handler {
        return acceptHandleNow(source) { item -> item.getItemTag() }
    }
}