package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.readInt
import top.lanscarlos.vulpecula.utils.readItemStack

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:03
 */
object ItemEnchantHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("enchantments", "enchantment", "enchants", "enchant")

    override fun read(isRoot: Boolean, reader: QuestReader): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        reader.mark()
        return when (reader.nextToken()) {
            "insert", "add",
            "modify", "set" -> modify(reader, source)
            "remove", "rm" -> remove(reader, source)
            "clear" -> clear(source)
            "has", "contains", "contain" -> contains(reader, source)
            "level" -> level(reader, source)
            else -> {
                reader.reset()
                acceptHandler(source) { item -> item.itemMeta?.enchants }
            }
        }
    }

    private fun modify(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val type = StringLiveData(reader.nextBlock())
        val level = reader.readInt()
        val ignoreLevelRestriction = !reader.hasNextToken("-restriction", "-r")

        return applyTransfer(source) { _, meta ->
            val enchant = type.runAsEnchantment(this) ?: return@applyTransfer meta

            meta.also { it.addEnchant(enchant, level.get(this, 1), ignoreLevelRestriction) }
        }
    }

    private fun remove(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val type = StringLiveData(reader.nextBlock())

        return applyTransfer(source) { _, meta ->
            val enchant = type.runAsEnchantment(this) ?: return@applyTransfer meta

            meta.also { it.removeEnchant(enchant) }
        }
    }

    private fun clear(source: LiveData<ItemStack>?): ActionItemStack.Handler {
        return applyTransfer(source) { _, meta ->
            meta.also { it.enchants.keys.forEach { enchant -> it.removeEnchant(enchant) } }
        }
    }

    private fun contains(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val type = if (!reader.hasNextToken("any")) StringLiveData(reader.nextBlock()) else null

        return acceptHandler(source) { item ->
            val itemMeta = item.itemMeta

            if (type == null) return@acceptHandler itemMeta?.hasEnchants() ?: false

            val enchant = type.runAsEnchantment(this) ?: return@acceptHandler false

            itemMeta?.hasEnchant(enchant) ?: false
        }
    }

    private fun level(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        reader.hasNextToken("by")
        val type = StringLiveData(reader.nextBlock())

        return acceptHandler(source) { item ->
            val itemMeta = item.itemMeta

            val enchant = type.runAsEnchantment(this) ?: return@acceptHandler 0

            itemMeta?.getEnchantLevel(enchant) ?: 0
        }
    }

    /**
     * 获取 Enchantment 对象
     * */
    private fun LiveData<String>.runAsEnchantment(frame: ScriptFrame): Enchantment? {
        val name = this.getOrNull(frame) ?: return null
        return Enchantment.values().firstOrNull { name.equals(it.name, true) }
    }
}