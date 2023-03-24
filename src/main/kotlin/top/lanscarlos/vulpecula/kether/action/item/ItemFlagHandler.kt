package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.inventory.ItemFlag
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.kether.live.readItemStack
import top.lanscarlos.vulpecula.kether.live.readString
import top.lanscarlos.vulpecula.utils.thenTake

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:15
 */
@Deprecated("")
object ItemFlagHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("flags", "flag")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        reader.mark()

        when (val next = reader.nextToken()) {
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

                return acceptTransferFuture(source) { item ->
                    raw.map { it.getOrNull(this) }.thenTake().thenApply { args ->
                        val meta = item.itemMeta ?: return@thenApply item
                        val flags = ItemFlag.values().filter { it.name in args }

                        when (next) {
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

                        return@thenApply item.also { it.itemMeta = meta }
                    }
                }
            }
            "clear" -> {
                return acceptTransferNow(source) { item ->
                    val meta = item.itemMeta ?: return@acceptTransferNow item

                    meta.itemFlags.forEach {
                        meta.removeItemFlags(it)
                    }

                    return@acceptTransferNow item.also { it.itemMeta = meta }
                }
            }
            "has", "contains", "contain" -> {
                val type = reader.readString()
                return acceptHandleFuture(source) { item ->
                    type.getOrNull(this).thenApply { name ->
                        val flag = ItemFlag.values().firstOrNull { it.name.equals(name, true) } ?: return@thenApply item
                        return@thenApply item.itemMeta?.hasItemFlag(flag) ?: false
                    }
                }
            }
            else -> {
                reader.reset()
                return acceptHandleNow(source) { item -> item.itemMeta?.itemFlags }
            }
        }
    }
}