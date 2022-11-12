package top.lanscarlos.vulpecula.kether.action

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
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

        val def: ItemStack get() = ItemStack(Material.STONE)

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
                            "name" -> name = option.value.getValue(this@transfer, "").ifEmpty { name }
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

            return transfer { previous ->
                val item = previous ?: itemStack?.get(this, def) ?: error("No item selected.")
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
                            val name = option.value.getValue(this@transfer, "").ifEmpty { null }
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
                item.itemMeta = itemMeta
                item
            }
        }

        fun lore(isRoot: Boolean, reader: QuestReader): Handler {
            val itemStack = if (isRoot) reader.readItemStack() else null

            return when (val it = reader.expects("insert", "add", "modify", "set", "remove", "rm", "clear")) {
                "insert", "add" -> {
                    val raw = reader.readStringList()
                    val index = if (reader.hasNextToken("to")) {
                        reader.readInt()
                    } else null

                    transfer { previous ->
                        val item = previous ?: itemStack?.get(this, def) ?: error("No item selected.")
                        val itemMeta = item.itemMeta
                        val lore = itemMeta?.lore ?: mutableListOf()

                        val newLore = raw.get(this, listOf())
                        val cursor = index.getValue(this, lore.size)
                        if (cursor >= lore.size) {
                            // 下标位于末尾
                            lore.addAll(newLore)
                        } else {
                            lore.addAll(cursor, newLore)
                        }

                        itemMeta?.lore = lore
                        item.itemMeta = itemMeta
                        item
                    }
                }
                "modify", "set" -> {
                    val index = reader.readInt()
                    reader.expect("to")
                    val raw = StringLiveData(reader.nextBlock())

                    transfer { previous ->
                        val item = previous ?: itemStack?.get(this, def) ?: error("No item selected.")
                        val itemMeta = item.itemMeta
                        val lore = itemMeta?.lore ?: mutableListOf()

                        val cursor = index.get(this, lore.size)
                        val line = raw.get(this, "").ifEmpty { null } ?: return@transfer item
                        if (cursor >= lore.size) {
                            // 下标位于末尾
                            lore.add(line)
                        } else {
                            lore[cursor] = line
                        }

                        itemMeta?.lore = lore
                        item.itemMeta = itemMeta
                        item
                    }
                }
                "remove", "rm" -> {
                    val index = reader.readInt()
                    transfer { previous ->
                        val item = previous ?: itemStack?.get(this, def) ?: error("No item selected.")
                        val itemMeta = item.itemMeta
                        val lore = itemMeta?.lore ?: mutableListOf()

                        val cursor = index.get(this, lore.size)
                        if (cursor >= lore.size) return@transfer item
                        lore.removeAt(cursor)

                        itemMeta?.lore = lore
                        item.itemMeta = itemMeta
                        item
                    }
                }
                "clear" -> {
                    transfer { previous ->
                        val item = previous ?: itemStack?.get(this, def) ?: error("No item selected.")
                        val itemMeta = item.itemMeta

                        if (itemMeta?.lore == null) return@transfer item
                        itemMeta.lore = null

                        item.itemMeta = itemMeta
                        item
                    }
                }
                else -> error("Unknown argument \"$it\" at item lore action.")
            }
        }

        private fun handle(func: ScriptFrame.(previous: ItemStack?) -> Any): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: ItemStack?): Any {
                    return func(frame, previous)
                }
            }
        }

        private fun transfer(func: ScriptFrame.(previous: ItemStack?) -> ItemStack): Handler {
            return object : TransferHandler {
                override fun handle(frame: ScriptFrame, previous: ItemStack?): ItemStack {
                    return func(frame, previous)
                }
            }
        }
    }
}