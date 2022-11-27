package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.inventory.ItemStack
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.internal.ClassInjector
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
        var previous: CompletableFuture<out ItemStack?> = CompletableFuture.completedFuture(null)
        for (handler in handlers) {
            if (handler is Transfer) {
                previous = handler.handle(frame, previous)
            } else {
                return handler.handle(frame, previous) as CompletableFuture<Any?>
            }
        }
        return previous as CompletableFuture<Any?>
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
            name = ["item", "itemstack"],
            override = ["item", "itemstack"]
        )
        fun parser() = scriptParser { reader ->
            val action = ActionItemStack()
            do {
                val it = reader.nextToken()
                val isRoot = action.handlers.isEmpty()

                action.handlers += registry[it]?.read(reader, it, isRoot) ?: error("Unknown argument \"$it\" at item action.")

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
        fun handle(frame: ScriptFrame, previous: CompletableFuture<out ItemStack?>): CompletableFuture<out Any?>
    }

    /**
     * 处理后返回 ItemStack 对象，供下一处理器使用
     * */
    interface Transfer : Handler {
        override fun handle(frame: ScriptFrame, previous: CompletableFuture<out ItemStack?>): CompletableFuture<out ItemStack>
    }

    /**
     * 读取语句
     * */
    interface Reader {

        val name: Array<String>

        /**
         * @param input 传入的 name
         * @param isRoot 是否为队列最前端
         * @return 处理器
         * */
        fun read(reader: QuestReader, input: String, isRoot: Boolean): Handler

        /**
         * 返回任意对象
         * */
        fun handleNow(func: ScriptFrame.(item: ItemStack?) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out ItemStack?>): CompletableFuture<out Any?> {
                    return if (previous.isDone) {
                        CompletableFuture.completedFuture(func(frame, previous.getNow(null)))
                    } else {
                        previous.thenApply { func(frame, it) }
                    }
                }
            }
        }

        /**
         * 返回任意对象
         * */
        fun handleFuture(func: ScriptFrame.(item: ItemStack?) -> CompletableFuture<Any?>): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out ItemStack?>): CompletableFuture<out Any?> {
                    return if (previous.isDone) {
                        func(frame, previous.getNow(null))
                    } else {
                        val future = CompletableFuture<Any>()
                        previous.thenAccept { item ->
                            func(frame, item).thenAccept {
                                future.complete(it)
                            }
                        }
                        return future
                    }
                }
            }
        }

        /**
         * 接收 ItemStack 返回任意对象
         * */
        fun acceptHandleNow(source: LiveData<ItemStack>?, func: ScriptFrame.(item: ItemStack) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out ItemStack?>): CompletableFuture<out Any?> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            CompletableFuture.completedFuture(
                                func(frame, wait.getNow(null) ?: error("No item selected."))
                            )
                        } else {
                            wait.thenApply {
                                func(frame, it ?: error("No item selected."))
                            }
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            CompletableFuture.completedFuture(
                                func(frame, previous.getNow(null) ?: error("No item selected."))
                            )
                        } else {
                            previous.thenApply {
                                func(frame, it ?: error("No item selected."))
                            }
                        }
                    }
                }
            }
        }

        /**
         * 接收 ItemStack 返回任意对象
         * */
        fun acceptHandleFuture(source: LiveData<ItemStack>?, func: ScriptFrame.(item: ItemStack) -> CompletableFuture<Any?>): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out ItemStack?>): CompletableFuture<out Any?> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            func(frame, wait.getNow(null) ?: error("No item selected."))
                        } else {
                            val future = CompletableFuture<Any?>()
                            wait.thenAccept { item ->
                                func(frame, item ?: error("No item selected.")).thenAccept {
                                    future.complete(it)
                                }
                            }
                            future
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            func(frame, previous.getNow(null) ?: error("No item selected."))
                        } else {
                            val future = CompletableFuture<Any?>()
                            previous.thenAccept { item ->
                                func(frame, item ?: error("No item selected.")).thenAccept {
                                    future.complete(it)
                                }
                            }
                            future
                        }
                    }
                }
            }
        }

        /**
         * 返回 ItemStack 对象
         * */
        fun transferNow(func: ScriptFrame.(item: ItemStack?) -> ItemStack): Handler {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out ItemStack?>): CompletableFuture<out ItemStack> {
                    return if (previous.isDone) {
                        CompletableFuture.completedFuture(func(frame, previous.getNow(null)))
                    } else {
                        previous.thenApply { func(frame, it) }
                    }
                }
            }
        }

        /**
         * 返回 ItemStack 对象
         * */
        fun transferFuture(func: ScriptFrame.(item: ItemStack?) -> CompletableFuture<ItemStack>): Handler {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out ItemStack?>): CompletableFuture<out ItemStack> {
                    return if (previous.isDone) {
                        func(frame, previous.getNow(null))
                    } else {
                        val future = CompletableFuture<ItemStack>()
                        previous.thenAccept { item ->
                            func(frame, item).thenAccept {
                                future.complete(it)
                            }
                        }
                        return future
                    }
                }
            }
        }

        /**
         * 接收 ItemStack 并返回 ItemStack 对象
         * */
        fun acceptTransferNow(source: LiveData<ItemStack>?, func: ScriptFrame.(item: ItemStack) -> ItemStack): Transfer {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out ItemStack?>): CompletableFuture<out ItemStack> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            CompletableFuture.completedFuture(
                                func(frame, wait.getNow(null) ?: error("No item selected."))
                            )
                        } else {
                            wait.thenApply {
                                func(frame, it ?: error("No item selected."))
                            }
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            CompletableFuture.completedFuture(
                                func(frame, previous.getNow(null) ?: error("No item selected."))
                            )
                        } else {
                            previous.thenApply {
                                func(frame, it ?: error("No item selected."))
                            }
                        }
                    }
                }
            }
        }

        /**
         * 接收 ItemStack 并返回 ItemStack 对象
         * */
        fun acceptTransferFuture(source: LiveData<ItemStack>?, func: ScriptFrame.(item: ItemStack) -> CompletableFuture<ItemStack>): Transfer {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out ItemStack?>): CompletableFuture<out ItemStack> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            func(frame, wait.getNow(null) ?: error("No item selected."))
                        } else {
                            val future = CompletableFuture<ItemStack>()
                            wait.thenAccept { item ->
                                func(frame, item ?: error("No item selected.")).thenAccept {
                                    future.complete(it)
                                }
                            }
                            future
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            func(frame, previous.getNow(null) ?: error("No item selected."))
                        } else {
                            val future = CompletableFuture<ItemStack>()
                            previous.thenAccept { item ->
                                func(frame, item ?: error("No item selected.")).thenAccept {
                                    future.complete(it)
                                }
                            }
                            future
                        }
                    }
                }
            }
        }
    }
}