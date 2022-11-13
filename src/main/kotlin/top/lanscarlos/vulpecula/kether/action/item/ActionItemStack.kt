package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.injector.ClassInjector
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextPeek
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-12 13:01
 */
class ActionItemStack : ScriptAction<Any?>() {

    private val handlers = mutableListOf<Handler>()

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        var previous: ItemStack? = null
        for (handler in handlers) {
            if (handler is Transfer) {
                previous = handler.handle(frame, previous)
            } else {
                return CompletableFuture.completedFuture(
                    handler.handle(frame, previous)
                )
            }
        }
        return CompletableFuture.completedFuture(previous)
    }

    companion object : ClassInjector(packageName = ActionItemStack::class.java.packageName) {

        private val registry = mutableMapOf<String, Reader>()

        fun registerReader(reader: Reader) {
            reader.name.forEach { registry[it] = reader }
        }

        override fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {
            if (!Reader::class.java.isAssignableFrom(clazz)) return

            val reader = let {
                if (supplier?.get() != null) {
                    supplier.get()
                } else try {
                    clazz.getDeclaredConstructor().newInstance()
                } catch (e: Exception) {
                    null
                }
            } as? Reader ?: return

            registerReader(reader)
        }

        @VulKetherParser(
            id = "item",
            name = ["item"],
            override = ["item", "itemstack"]
        )
        fun parser() = scriptParser { reader ->
            val action = ActionItemStack()
            do {
                val it = reader.nextToken()
                val isRoot = action.handlers.isEmpty()

                action.handlers += registry[it]?.read(isRoot, reader) ?: error("Unknown argument \"$it\" at item action.")

                // 判断管道是否已关闭
                if (action.handlers.lastOrNull() !is Transfer) {
                    if (reader.hasNextToken(">>")) {
                        error("Cannot use \">> ${reader.nextPeek()}\", previous action \"$it\" has closed the pipeline.")
                    }
                    break
                }
            } while (reader.hasNextToken(">>"))

            return@scriptParser action
        }
    }

    /**
     * 处理后返回任意对象
     * */
    interface Handler {
        fun handle(frame: ScriptFrame, previous: ItemStack?): Any?
    }

    /**
     * 处理后返回 ItemStack 对象，供下一处理器使用
     * */
    interface Transfer : Handler {
        override fun handle(frame: ScriptFrame, previous: ItemStack?): ItemStack
    }

    /**
     * 读取语句
     * */
    interface Reader {

        val name: Array<String>

        /**
         * @param isRoot 是否为队列最前端
         * @return 处理器
         * */
        fun read(isRoot: Boolean, reader: QuestReader): Handler

        /**
         * 返回任意对象
         * */
        fun handle(func: ScriptFrame.(item: ItemStack?) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: ItemStack?): Any? {
                    return func(frame, previous)
                }
            }
        }

        /**
         * 接收 ItemStack 返回任意对象
         * */
        fun acceptHandler(source: LiveData<ItemStack>?, func: ScriptFrame.(item: ItemStack) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: ItemStack?): Any? {
                    val item = previous ?: source?.getOrNull(frame) ?: error("No item select.")
                    return func(frame, item)
                }
            }
        }

        /**
         * 返回 ItemStack 对象
         * */
        fun transfer(func: ScriptFrame.(item: ItemStack?) -> ItemStack): Handler {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: ItemStack?): ItemStack {
                    return func(frame, previous)
                }
            }
        }

        /**
         * 接收 ItemStack 并返回 ItemStack 对象
         * */
        fun acceptTransfer(source: LiveData<ItemStack>?, func: ScriptFrame.(item: ItemStack) -> ItemStack): Transfer {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: ItemStack?): ItemStack {
                    val item = previous ?: source?.getOrNull(frame) ?: error("No item select.")
                    return func(frame, item)
                }
            }
        }

        /**
         * 接收 ItemStack 和 ItemMeta 并返回 ItemMeta 对象
         * */
        fun applyTransfer(source: LiveData<ItemStack>?, func: ScriptFrame.(item: ItemStack, meta: ItemMeta) -> ItemMeta?): Transfer {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: ItemStack?): ItemStack {
                    val item = previous ?: source?.getOrNull(frame) ?: error("No item select.")
                    val meta = func(frame, item, item.itemMeta ?: return item)
                    return item.also { it.itemMeta = meta }
                }
            }
        }
    }
}