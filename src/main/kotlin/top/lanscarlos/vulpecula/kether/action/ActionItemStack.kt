package top.lanscarlos.vulpecula.kether.action

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.util.addSafely
import taboolib.common.util.setSafely
import taboolib.library.kether.QuestReader
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.expects
import taboolib.module.kether.scriptParser
import taboolib.platform.util.buildItem
import taboolib.platform.util.modifyLore
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-12 13:01
 */
class ActionItemStack : ScriptAction<Any?>() {

    interface Handler {
        fun handle(frame: ScriptFrame, previous: ItemStack?): Any
    }

    interface TransferHandler : Handler {
        override fun handle(frame: ScriptFrame, previous: ItemStack?): ItemStack
    }

    private val handlers = mutableListOf<Handler>()

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        var previous: ItemStack? = null
        for (handler in handlers) {
            if (handler is TransferHandler) {
                previous = handler.handle(frame, previous)
            } else {
                return CompletableFuture.completedFuture(
                    handler.handle(frame, previous)
                )
            }
        }
        return CompletableFuture.completedFuture(previous)
    }

    companion object {

        /**
         *
         * item build &type -name(n) &name -lore &lore -amount...
         *
         * */
        @VulKetherParser(
            id = "item",
            name = ["item"],
            override = ["item", "itemstack"]
        )
        fun parser() = scriptParser { reader ->
            val action = ActionItemStack()
            do {
                val isRoot = action.handlers.isEmpty()
                val it = reader.nextToken()
                action.handlers += when (it) {
                    "build" -> build(reader)
                    "modify", "set" -> modify(isRoot, reader)
                    "lore" -> lore(isRoot, reader)
                    "enchant" -> enchant(isRoot, reader)
                    "flag" -> flag(isRoot, reader)
                    else -> error("Unknown argument \"$it\" at item action.")
                }
                if (action.handlers.lastOrNull() !is TransferHandler) {
                    if (reader.hasNextToken(">>")) {
                        error("Cannot use \">> ${reader.nextPeek()}\", previous action \"$it\" has closed the pipeline.")
                    }
                    break
                }
            } while (reader.hasNextToken(">>"))

            return@scriptParser action
        }

        fun build(reader: QuestReader): Handler {
            val type = StringLiveData(reader.nextBlock())
            val options = mutableMapOf<String, LiveData<*>>()
            while (reader.nextPeek().startsWith('-')) {
                when (val it = reader.nextToken().substring(1)) {
                    "amount", "amt", "a" -> options["amount"] = reader.readInt()
                    "durability", "dura" -> options["durability"] = reader.readInt()
                    "name", "n" -> options["name"] = StringLiveData(reader.nextBlock())
                    "shiny" -> options["shiny"] = reader.readBoolean()
                    "colored" -> options["colored"] = reader.readBoolean()
                    "customModelData", "model" -> options["model"] = reader.readInt()
                    else -> error("Unknown argument \"$it\" at item build action.")
                }
            }

            return transfer {
                val material = XMaterial.matchXMaterial(type.get(this, "STONE").uppercase()).let {
                    if (it.isPresent) it.get() else XMaterial.STONE
                }
                buildItem(material) {
                    for (option in options) {
                        when (option.key) {
                            "amount" -> amount = option.value.getValue(this@transfer, amount)
                            "durability" -> damage = option.value.getValue(this@transfer, damage)
                            "name" -> name = option.value.getValueOrNull<String>(this@transfer) ?: name
                            "shiny" -> {
                                if (option.value.getValue(this@transfer, false)) shiny()
                            }
                            "colored" -> {
                                if (option.value.getValue(this@transfer, true)) colored()
                            }
                            "model" -> customModelData = option.value.getValue(this@transfer, customModelData)
                        }
                    }
                }
            }
        }

        fun modify(isRoot: Boolean, reader: QuestReader): Handler {
            val itemStack = if (isRoot) reader.readItemStack() else null
            val options = mutableMapOf<String, LiveData<*>>()
            while (reader.nextPeek().startsWith('-')) {
                when (val it = reader.nextToken().substring(1)) {
                    "material", "mat", "type" -> options["material"] = StringLiveData(reader.nextBlock())
                    "amount", "amt", "a" -> options["amount"] = reader.readInt()
                    "durability", "dura" -> options["durability"] = reader.readInt()
                    "name", "n" -> options["name"] = StringLiveData(reader.nextBlock())
                    "customModelData", "model" -> options["model"] = reader.readInt()
                    else -> error("Unknown argument \"$it\" at item build action.")
                }
            }

            return transfer(itemStack) { item ->
                val itemMeta = item.itemMeta
                for (option in options) {
                    when (option.key) {
                        "material" -> {
                            val mat = option.value.getValue(this, item.type.name).uppercase()
                            item.type = XMaterial.matchXMaterial(mat).let {
                                if (it.isPresent) it.get().parseMaterial() else null
                            } ?: item.type
                        }
                        "amount" -> item.amount = option.value.getValue(this@transfer, item.amount)
                        "durability" -> item.durability = option.value.getValue(this@transfer, item.durability)
                        "name" -> {
                            val name = option.value.getValueOrNull<String>(this@transfer)
                            itemMeta?.setDisplayName(name?.colored())
                        }
                        "model" -> {
                            val customModelData = option.value.getValue(this@transfer, -1)
                            try {
                                if (customModelData != -1) {
                                    itemMeta?.invokeMethod<Void>("setCustomModelData", customModelData)
                                }
                            } catch (ignored: NoSuchMethodException) {
                            }
                        }
                    }
                }
                item.also { it.itemMeta = itemMeta }
            }
        }

        fun lore(isRoot: Boolean, reader: QuestReader): Handler {
            val itemStack = if (isRoot) reader.readItemStack() else null

            return when (val it = reader.expects(
                "insert", "add",
                "modify", "set",
                "remove", "rm",
                "clear"
            )) {
                "insert", "add" -> {
                    val raw = reader.readStringList()
                    val index = if (reader.hasNextToken("to")) {
                        reader.readInt()
                    } else null

                    transferMeta(itemStack) { itemMeta ->
                        val lore = itemMeta.lore ?: mutableListOf()

                        val newLore = raw.get(this, listOf())
                        val cursor = index?.getOrNull(this) ?: lore.size
                        if (cursor >= lore.size) {
                            // 下标位于末尾
                            lore.addAll(newLore)
                        } else {
                            lore.addAll(cursor, newLore)
                        }

                        itemMeta.also { it.lore = lore }
                    }
                }
                "modify", "set" -> {
                    val index = reader.readInt()
                    reader.hasNextToken("to")
                    val raw = StringLiveData(reader.nextBlock())

                    transferMeta(itemStack) { itemMeta ->
                        val lore = itemMeta.lore ?: mutableListOf()

                        val cursor = index.get(this, lore.size)
                        val line = raw.getOrNull(this) ?: return@transferMeta itemMeta
                        if (cursor >= lore.size) {
                            // 下标位于末尾
                            lore.add(line)
                        } else {
                            lore[cursor] = line
                        }

                        itemMeta.also { it.lore = lore }
                    }
                }
                "remove", "rm" -> {
                    val index = reader.readInt()
                    transferMeta(itemStack) { itemMeta ->
                        val lore = itemMeta.lore ?: mutableListOf()

                        val cursor = index.get(this, lore.size)
                        if (cursor >= lore.size) return@transferMeta itemMeta
                        lore.removeAt(cursor)

                        itemMeta.also { it.lore = lore }
                    }
                }
                "clear" -> {
                    transferMeta(itemStack) { itemMeta ->
                        itemMeta.also { it.lore = null }
                    }
                }
                else -> error("Unknown argument \"$it\" at item lore action.")
            }
        }

        fun enchant(isRoot: Boolean, reader: QuestReader): Handler {
            val itemStack = if (isRoot) reader.readItemStack() else null
            return when (val it = reader.expects(
                "insert", "add",
                "modify", "set",
                "remove", "rm",
                "clear",
                "has", "contains", "contain",
                "level"
            )) {
                "insert", "add",
                "modify", "set" -> {
                    val type = StringLiveData(reader.nextBlock())
                    val level = reader.readInt()
                    val ignoreLevelRestriction = !reader.hasNextToken("-restriction", "-r")

                    transferMeta(itemStack) { itemMeta ->
                        val name = type.getOrNull(this) ?: return@transferMeta itemMeta
                        val enchant = Enchantment.values().firstOrNull {
                            name.equals(it.name, true)
                        } ?: return@transferMeta itemMeta

                        itemMeta.also {
                            it.addEnchant(enchant, level.get(this, 1), ignoreLevelRestriction)
                        }
                    }
                }
                "remove", "rm" -> {
                    val type = StringLiveData(reader.nextBlock())
                    transferMeta(itemStack) { itemMeta ->

                        val name = type.getOrNull(this) ?: return@transferMeta itemMeta
                        val enchant = Enchantment.values().firstOrNull {
                            name.equals(it.name, true)
                        } ?: return@transferMeta itemMeta

                        itemMeta.also { it.removeEnchant(enchant) }
                    }
                }
                "clear" -> {
                    transferMeta(itemStack) { itemMeta ->
                        itemMeta.also {
                            it.enchants.keys.forEach { enchant -> it.removeEnchant(enchant) }
                        }
                    }
                }
                "has", "contains", "contain" -> {
                    val type = if (!reader.hasNextToken("any")) StringLiveData(reader.nextBlock()) else null
                    handle(itemStack) { previous ->
                        val itemMeta = previous.itemMeta

                        if (type == null) return@handle itemMeta?.hasEnchants() ?: false

                        val name = type.getOrNull(this) ?: return@handle false
                        val enchant = Enchantment.values().firstOrNull {
                            name.equals(it.name, true)
                        } ?: return@handle false

                        return@handle itemMeta?.hasEnchant(enchant) ?: false
                    }
                }
                "level" -> {
                    reader.hasNextToken("by")
                    val type = StringLiveData(reader.nextBlock())
                    handle(itemStack) { previous ->
                        val itemMeta = previous.itemMeta

                        val name = type.getOrNull(this) ?: return@handle 0
                        val enchant = Enchantment.values().firstOrNull {
                            name.equals(it.name, true)
                        } ?: return@handle 0

                        return@handle itemMeta?.getEnchantLevel(enchant) ?: 0
                    }
                }
                else -> error("Unknown argument \"$it\" at item enchant action.")
            }
        }

        fun flag(isRoot: Boolean, reader: QuestReader): Handler {
            val itemStack = if (isRoot) reader.readItemStack() else null

            if (reader.hasNextToken("clear")) {
                return transferMeta(itemStack) { itemMeta ->
                    itemMeta.also {
                        it.itemFlags.forEach { flag -> it.removeItemFlags(flag) }
                    }
                }
            } else {
                val operation = reader.expects(
                    "insert", "add",
                    "modify", "set",
                    "remove", "rm",
                )

                // 解析 Flags
                val raw = mutableSetOf<LiveData<String>>()
                if (reader.hasNextToken("[")) {
                    while (!reader.hasNextToken("]")) {
                        raw += reader.readString()
                    }
                } else {
                    raw += reader.readString()
                }

                return transferMeta(itemStack) { itemMeta ->
                    val flags = raw.mapNotNull {
                        it.getOrNull(this)
                    }.map {
                        ItemFlag.valueOf(it)
                    }

                    when (operation) {
                        "insert", "add" -> {
                            itemMeta.addItemFlags(*flags.toTypedArray())
                        }
                        "modify", "set" -> {
                            itemMeta.itemFlags.forEach {
                                if (it !in flags) itemMeta.removeItemFlags(it)
                            }
                            itemMeta.addItemFlags(*flags.toTypedArray())
                        }
                        "remove", "rm" -> {
                            itemMeta.itemFlags.forEach {
                                if (it !in flags) itemMeta.removeItemFlags(it)
                            }
                        }
                    }
                    return@transferMeta itemMeta
                }
            }
        }

        private fun handle(itemStack: LiveData<ItemStack>? = null, func: ScriptFrame.(previous: ItemStack) -> Any): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: ItemStack?): Any {
                    val item = previous ?: itemStack?.getOrNull(frame) ?: error("No item select.")
                    return func(frame, item)
                }
            }
        }

        private fun transfer(itemStack: LiveData<ItemStack>? = null, func: ScriptFrame.(previous: ItemStack) -> ItemStack): Handler {
            return object : TransferHandler {
                override fun handle(frame: ScriptFrame, previous: ItemStack?): ItemStack {
                    val item = previous ?: itemStack?.getOrNull(frame) ?: error("No item select.")
                    return func(frame, item)
                }
            }
        }

        private fun transferMeta(itemStack: LiveData<ItemStack>? = null, func: ScriptFrame.(itemMeta: ItemMeta) -> ItemMeta): Handler {
            return object : TransferHandler {
                override fun handle(frame: ScriptFrame, previous: ItemStack?): ItemStack {
                    val item = previous ?: itemStack?.getOrNull(frame) ?: error("No item select.")
                    val meta = item.itemMeta ?: error("No item meta select.")
                    item.itemMeta = func(frame, meta)
                    return item
                }
            }
        }
    }
}