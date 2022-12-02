package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.readInt
import top.lanscarlos.vulpecula.kether.live.readItemStack
import top.lanscarlos.vulpecula.kether.live.readString
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:03
 */
object ItemEnchantHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("enchantments", "enchantment", "enchants", "enchant")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        reader.mark()
        return when (reader.nextToken()) {
            "insert", "add",
            "modify", "set" -> modify(reader, source)
            "remove", "rm" -> remove(reader, source)
            "clear" -> clear(source)
            "has", "contains", "contain" -> contains(reader, source)
            "level", "lvl" -> level(reader, source)
            else -> {
                reader.reset()
                acceptHandleNow(source) { item -> item.itemMeta?.enchants }
            }
        }
    }

    private fun modify(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val type = reader.readString()
        reader.hasNextToken("to")
        val level = reader.readInt()
        val ignoreLevelRestriction = !reader.hasNextToken("-restriction", "-r")

        return acceptTransferFuture(source) { item ->
            listOf(
                type.getOrNull(this),
                level.getOrNull(this)
            ).thenTake().thenApply { args ->
                val meta = item.itemMeta ?: return@thenApply item
                val enchant = args[0]?.toString()?.asEnchantment() ?: return@thenApply item

                meta.addEnchant(enchant, args[1].coerceInt(1), ignoreLevelRestriction)

                return@thenApply item.also { it.itemMeta = meta }
            }
        }
    }

    private fun remove(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val type = reader.readString()

        return acceptTransferFuture(source) { item ->
            type.getOrNull(this).thenApply { name ->
                val meta = item.itemMeta ?: return@thenApply item
                val enchant = name?.asEnchantment() ?: return@thenApply item

                meta.removeEnchant(enchant)

                return@thenApply item.also { it.itemMeta = meta }
            }
        }
    }

    private fun clear(source: LiveData<ItemStack>?): ActionItemStack.Handler {
        return acceptTransferNow(source) { item ->
            val meta = item.itemMeta ?: return@acceptTransferNow item
            meta.enchants.keys.forEach { meta.removeEnchant(it) }

            return@acceptTransferNow item.also { it.itemMeta = meta }
        }
    }

    private fun contains(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        if (reader.hasNextToken("any")) {
            return acceptHandleNow(source) { item ->
                item.itemMeta?.hasEnchants() ?: false
            }
        } else {
            val type = reader.readString()
            return acceptHandleFuture(source) { item ->
                type.getOrNull(this).thenApply { name ->
                    val enchant = name?.asEnchantment() ?: return@thenApply false

                    return@thenApply item.itemMeta?.hasEnchant(enchant) ?: false
                }
            }
        }
    }

    private fun level(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        reader.hasNextToken("by")
        val type = reader.readString()

        return acceptHandleFuture(source) { item ->
            type.getOrNull(this).thenApply { name ->
                val enchant = name?.asEnchantment() ?: return@thenApply 0

                return@thenApply item.itemMeta?.getEnchantLevel(enchant) ?: 0
            }
        }
    }

    fun String.asEnchantment(): Enchantment? {
        return Enchantment.values().firstOrNull { it.key.key.equals(this, true) }
    }
}