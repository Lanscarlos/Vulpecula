package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.inventory.ItemStack
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 19:42
 */
object ItemLoreHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("lore")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        reader.mark()
        return when (reader.nextToken()) {
            "insert", "add" -> insert(reader, source)
            "modify", "set" -> modify(reader, source)
            "remove", "rm" -> remove(reader, source)
            "clear" -> clear(source)
            else -> {
                reader.reset()
                acceptHandleNow(source) { item -> item.itemMeta?.lore }
            }
        }
    }

    private fun insert(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val raw = reader.readStringList()
        val index = if (reader.hasNextToken("to")) reader.readInt() else null

        return acceptTransferFuture(source) { item ->
            listOf(
                raw.getOrNull(this),
                index?.getOrNull(this)
            ).thenTake().thenApply { args ->
                val meta = item.itemMeta ?: return@thenApply item
                val lore = meta.lore ?: mutableListOf()

                val list = (args[0] as? List<*>)?.map { it?.toString() } ?: return@thenApply item
                val cursor = args[1]?.toInt() ?: lore.size
                if (cursor >= lore.size) {
                    // 下标位于末尾
                    lore.addAll(list)
                } else {
                    lore.addAll(cursor, list)
                }

                meta.lore = lore
                return@thenApply item.also { it.itemMeta = meta }
            }
        }
    }

    private fun modify(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val index = reader.readInt()
        reader.hasNextToken("to")
        val raw = StringLiveData(reader.nextBlock())

        return acceptTransferFuture(source) { item ->

            listOf(
                index.getOrNull(this),
                raw.getOrNull(this)
            ).thenTake().thenApply { args ->
                val meta = item.itemMeta ?: return@thenApply item
                val lore = meta.lore ?: mutableListOf()

                val cursor = args[0]?.toInt() ?: lore.size
                val line = args[1]?.toString()
                if (cursor >= lore.size) {
                    // 下标位于末尾
                    lore.add(line)
                } else {
                    lore[cursor] = line
                }

                meta.lore = lore
                return@thenApply item.also { it.itemMeta = meta }
            }
        }
    }

    private fun remove(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val index = reader.readInt()

        return acceptTransferFuture(source) { item ->
            index.getOrNull(this).thenApply { cursor ->
                val meta = item.itemMeta ?: return@thenApply item
                val lore = meta.lore ?: mutableListOf()

                if (cursor != null && cursor >= 0 && cursor < lore.size) {
                    lore.removeAt(cursor)
                } else {
                    lore.removeLast()
                }

                meta.lore = lore
                return@thenApply item.also { it.itemMeta = meta }
            }
        }
    }

    private fun clear(source: LiveData<ItemStack>?): ActionItemStack.Handler {
        return acceptTransferNow(source) { item ->
            val meta = item.itemMeta?.also { it.lore = null }
            return@acceptTransferNow item.also { it.itemMeta = meta }
        }
    }
}