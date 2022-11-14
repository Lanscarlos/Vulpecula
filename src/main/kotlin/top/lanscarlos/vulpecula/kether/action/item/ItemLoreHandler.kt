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
                acceptHandler(source) { item -> item.itemMeta?.lore }
            }
        }
    }

    private fun insert(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val raw = reader.readStringList()
        val index = if (reader.hasNextToken("to")) reader.readInt() else null

        return applyTransfer(source) { _, meta ->
            val lore = meta.lore ?: mutableListOf()

            val list = raw.get(this, listOf())
            val cursor = index?.getOrNull(this) ?: lore.size
            if (cursor >= lore.size) {
                // 下标位于末尾
                lore.addAll(list)
            } else {
                lore.addAll(cursor, list)
            }

            meta.also { it.lore = lore }
        }
    }

    private fun modify(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val index = reader.readInt()
        reader.hasNextToken("to")
        val raw = StringLiveData(reader.nextBlock())

        return applyTransfer(source) { _, meta ->
            val lore = meta.lore ?: mutableListOf()

            val cursor = index.get(this, lore.size)
            val line = raw.getOrNull(this) ?: return@applyTransfer meta
            if (cursor >= lore.size) {
                // 下标位于末尾
                lore.add(line)
            } else {
                lore[cursor] = line
            }

            meta.also { it.lore = lore }
        }
    }

    private fun remove(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val index = reader.readInt()

        return applyTransfer(source) { _, meta ->
            val lore = meta.lore ?: mutableListOf()

            val cursor = index.get(this, lore.size)
            if (cursor >= lore.size) return@applyTransfer meta
            lore.removeAt(cursor)

            meta.also { it.lore = lore }
        }
    }

    private fun clear(source: LiveData<ItemStack>?): ActionItemStack.Handler {
        return applyTransfer(source) { _, meta ->
            meta.also { it.lore = null }
        }
    }
}