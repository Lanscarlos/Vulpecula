package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.inventory.ItemFlag
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.readItemStack
import top.lanscarlos.vulpecula.utils.readString

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:15
 */
object ItemFlagHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("flags", "flag")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        reader.mark()

        return when (val it = reader.nextToken()) {
            "insert", "add",
            "modify", "set",
            "remove", "rm" -> {

                // 读取 flags
                val raw = mutableSetOf<LiveData<String>>()
                if (reader.hasNextToken("[")) {
                    while (!reader.hasNextToken("]")) {
                        raw += reader.readString()
                    }
                } else {
                    raw += reader.readString()
                }

                applyTransfer(source) { _, meta ->

                    // 加载 flags
                    val flags = raw.mapNotNull {
                        it.getOrNull(this)
                    }.map {
                        ItemFlag.valueOf(it)
                    }

                    when (it) {
                        "insert", "add" -> {
                            meta.addItemFlags(*flags.toTypedArray())
                        }
                        "modify", "set" -> {
                            meta.itemFlags.forEach {
                                if (it !in flags) meta.removeItemFlags(it)
                            }
                            meta.addItemFlags(*flags.toTypedArray())
                        }
                        "remove", "rm" -> {
                            meta.itemFlags.forEach {
                                if (it !in flags) meta.removeItemFlags(it)
                            }
                        }
                    }

                    return@applyTransfer meta
                }
            }
            "clear" -> {
                applyTransfer(source) { _, meta ->
                    meta.itemFlags.forEach {
                        meta.removeItemFlags(it)
                    }
                    return@applyTransfer meta
                }
            }
            "has", "contains", "contain" -> {
                val type = reader.readString()
                acceptHandler(source) { item ->

                    val flag = type.getOrNull(this)?.let {
                        ItemFlag.valueOf(it)
                    } ?: return@acceptHandler false

                    item.itemMeta?.hasItemFlag(flag)
                }
            }
            else -> {
                reader.reset()
                acceptHandler(source) { item -> item.itemMeta?.itemFlags }
            }
        }
    }
}