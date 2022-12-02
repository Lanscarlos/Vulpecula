package top.lanscarlos.vulpecula.kether.action.target

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.kether.live.readCollection
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.target
 *
 * @author Lanscarlos
 * @since 2022-11-16 21:37
 */
class ActionTarget : ScriptAction<Any>() {

    private val handlers = mutableListOf<Handler>()

    override fun run(frame: ScriptFrame): CompletableFuture<Any> {
        var collection: CompletableFuture<MutableCollection<Any>> = CompletableFuture.completedFuture(mutableSetOf())
        for (handler in handlers) {
            collection = handler.handle(frame, collection)
        }

        return collection.thenApply {
            if (it.size == 1) it.single() else it
        }
    }

    @Awake(LifeCycle.LOAD)
    companion object : ClassVisitor(1) {

        override fun getLifeCycle() = LifeCycle.LOAD

        private val selectors = mutableMapOf<String, Reader>()
        private val filters = mutableMapOf<String, Reader>()

        fun registerSelector(selector: Reader) {
            selector.name.forEach { selectors[it.lowercase()] = selector }
        }

        fun registerFilter(filter: Reader) {
            filter.name.forEach { filters[it.lowercase()] = filter }
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

            // 注册
            val type = reader::class.java.`package`.name.split('.').lastOrNull() ?: return
            when (type) {
                "selector" -> registerSelector(reader)
                "filter" -> registerFilter(reader)
            }
        }

        @VulKetherParser(
            id = "target",
            name = ["target"]
        )
        fun parser() = scriptParser { reader ->
            val action = ActionTarget()

            do {
                val next = reader.nextToken()
                val isRoot = action.handlers.isEmpty()

                val handler = if (next.startsWith('@')) {
                    val input = next.substring(1)
                    selectors[input.lowercase()]?.read(reader, input, isRoot)
                } else {
                    reader.mark()
                    val input = reader.nextToken()
                    when (next) {
                        "selector", "select", "sel" -> selectors[input.lowercase()]?.read(reader, input, isRoot)
                        "filter" -> filters[input.lowercase()]?.read(reader, input, isRoot)
                        "foreach" -> {
                            reader.reset()
                            TargetForEachHandler.read(reader, input, isRoot)
                        }
                        else -> {
                            selectors[input.lowercase()]?.read(reader, input, isRoot)
                        }
                    } ?: error("Unknown argument \"$next $input\" at target action.")
                }

                action.handlers += handler ?: error("Unknown argument \"$next\" at target action.")
            } while (reader.hasNextToken(">>"))

            return@scriptParser action
        }
    }

    /**
     * 处理后返回 Collection 对象
     * */
    interface Handler {
        fun handle(frame: ScriptFrame, previous: CompletableFuture<MutableCollection<Any>>): CompletableFuture<MutableCollection<Any>>
    }

    interface Reader {

        val name: Array<String>

        fun read(reader: QuestReader, input: String, isRoot: Boolean): Handler

        fun QuestReader.source(isRoot: Boolean): LiveData<Collection<*>>? {
            return if (isRoot) this.readCollection() else null
        }

        /**
         * 处理后返回 Collection 对象
         * */
        fun handleNow(func: ScriptFrame.(MutableCollection<Any>) -> MutableCollection<Any>): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<MutableCollection<Any>>): CompletableFuture<MutableCollection<Any>> {
                    return if (previous.isDone) {
                        CompletableFuture.completedFuture(func(frame, previous.getNow(null)))
                    } else {
                        previous.thenApply { func(frame, it) }
                    }
                }
            }
        }

        /**
         * 处理后返回 Collection 对象
         * */
        fun handleFuture(func: ScriptFrame.(MutableCollection<Any>) -> CompletableFuture<MutableCollection<Any>>): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<MutableCollection<Any>>): CompletableFuture<MutableCollection<Any>> {
                    if (previous.isDone) {
                        return func(frame, previous.getNow(null))
                    } else {
                        val future = CompletableFuture<MutableCollection<Any>>()
                        previous.thenAccept { collection ->
                            func(frame, collection).thenAccept {
                                future.complete(it)
                            }
                        }
                        return future
                    }
                }
            }
        }

        /**
         * 接收 Collection 并返回 Collection 对象
         * */
        fun acceptHandlerNow(source: LiveData<Collection<*>>?, func: ScriptFrame.(collection: MutableCollection<Any>) -> MutableCollection<Any>): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<MutableCollection<Any>>): CompletableFuture<MutableCollection<Any>> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            CompletableFuture.completedFuture(
                                func(frame, wait.getNow(null)?.filterNotNull()?.toMutableSet() ?: mutableSetOf())
                            )
                        } else {
                            wait.thenApply {
                                func(frame, it?.filterNotNull()?.toMutableSet() ?: mutableSetOf())
                            }
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            CompletableFuture.completedFuture(
                                func(frame, previous.getNow(null) ?: mutableSetOf())
                            )
                        } else {
                            previous.thenApply {
                                func(frame, it ?: mutableSetOf())
                            }
                        }
                    }
                }
            }
        }

        /**
         * 接收 Collection 并返回 Collection 对象
         * */
        fun acceptHandlerFuture(source: LiveData<Collection<*>>?, func: ScriptFrame.(collection: MutableCollection<Any>) -> CompletableFuture<MutableCollection<Any>>): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<MutableCollection<Any>>): CompletableFuture<MutableCollection<Any>> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            func(frame, wait.getNow(null)?.filterNotNull()?.toMutableSet() ?: mutableSetOf())
                        } else {
                            val future = CompletableFuture<MutableCollection<Any>>()
                            wait.thenAccept { collection ->
                                func(frame, collection?.filterNotNull()?.toMutableSet() ?: mutableSetOf()).thenAccept {
                                    future.complete(it)
                                }
                            }
                            future
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            func(frame, previous.getNow(null) ?: mutableSetOf())
                        } else {
                            val future = CompletableFuture<MutableCollection<Any>>()
                            previous.thenAccept { collection ->
                                func(frame, collection ?: mutableSetOf()).thenAccept {
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