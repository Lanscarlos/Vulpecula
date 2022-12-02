package top.lanscarlos.vulpecula.kether.action.vector

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.VectorLiveData
import top.lanscarlos.vulpecula.kether.live.readDouble
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.vector
 *
 * @author Lanscarlos
 * @since 2022-11-11 13:33
 */
class ActionVector : ScriptAction<Any?>() {

    private val handlers = mutableListOf<Handler>()

    @Suppress("UNCHECKED_CAST")
    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        var previous: CompletableFuture<out Vector?> = CompletableFuture.completedFuture(null)
        for (handler in handlers) {
            if (handler is Transfer) {
                previous = handler.handle(frame, previous)
            } else {
                return handler.handle(frame, previous) as CompletableFuture<Any?>
            }
        }
        return previous as CompletableFuture<Any?>
    }

    @Awake(LifeCycle.LOAD)
    companion object : ClassVisitor(1) {

        override fun getLifeCycle() = LifeCycle.LOAD

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
            id = "vector",
            name = ["vec", "vector"]
        )
        fun parser() = scriptParser { reader ->
            val action = ActionVector()
            do {
                reader.mark()
                val it = reader.nextToken()
                val isRoot = action.handlers.isEmpty()

                action.handlers += registry[it]?.read(reader, it, isRoot) ?: let { _ ->
                    reader.reset()
                    VectorBuildHandler.read(reader, it, isRoot)
                }

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
        fun handle(frame: ScriptFrame, previous: CompletableFuture<out Vector?>): CompletableFuture<out Any?>
    }

    /**
     * 处理后返回 Vector 对象，供下一处理器使用
     * */
    interface Transfer : Handler {
        override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Vector?>): CompletableFuture<out Vector>
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
         * 是否复制 Vector
         * */
        fun QuestReader.isReproduced(): Boolean {
            return !this.hasNextToken("not-reproduced", "not-rep", "not-clone", "-n")
        }

        /**
         * 读取期望 Vector 数据
         * 主要用于运算
         *
         * @param expect 期望前缀，若找不到则使用 other 来构建 Vector
         * @param other 用来标识构建 Vector 的前缀
         * */
        fun QuestReader.expectVector(expect: String, other: String): LiveData<Vector> {
            return if (this.hasNextToken(expect)) {
                VectorLiveData(this.nextBlock())
            } else {
                this.expect(other)
                val x = this.readDouble()
                val y = this.readDouble()
                val z = this.readDouble()
                VectorLiveData(Triple(x, y, z))
            }
        }

        /**
         * 返回任意对象
         * */
        fun handleNow(func: ScriptFrame.(vector: Vector?) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Vector?>): CompletableFuture<out Any?> {
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
        fun handleFuture(func: ScriptFrame.(vector: Vector?) -> CompletableFuture<Any?>): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Vector?>): CompletableFuture<out Any?> {
                    return if (previous.isDone) {
                        func(frame, previous.getNow(null))
                    } else {
                        val future = CompletableFuture<Any>()
                        previous.thenAccept { vector ->
                            func(frame, vector).thenAccept {
                                future.complete(it)
                            }
                        }
                        return future
                    }
                }
            }
        }

        /**
         * 接收 Vector 返回任意对象
         * */
        fun acceptHandleNow(source: LiveData<Vector>?, func: ScriptFrame.(vector: Vector) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Vector?>): CompletableFuture<out Any?> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            CompletableFuture.completedFuture(
                                func(frame, wait.getNow(null) ?: error("No vector select."))
                            )
                        } else {
                            wait.thenApply {
                                func(frame, it ?: error("No vector select."))
                            }
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            CompletableFuture.completedFuture(
                                func(frame, previous.getNow(null) ?: error("No vector select."))
                            )
                        } else {
                            previous.thenApply {
                                func(frame, it ?: error("No vector select."))
                            }
                        }
                    }
                }
            }
        }

        /**
         * 接收 Vector 返回任意对象
         * */
        fun acceptHandleFuture(source: LiveData<Vector>?, func: ScriptFrame.(vector: Vector) -> CompletableFuture<Any?>): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Vector?>): CompletableFuture<out Any?> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            func(frame, wait.getNow(null) ?: error("No vector select."))
                        } else {
                            val future = CompletableFuture<Any?>()
                            wait.thenAccept { vector ->
                                func(frame, vector ?: error("No vector select.")).thenAccept {
                                    future.complete(it)
                                }
                            }
                            future
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            func(frame, previous.getNow(null) ?: error("No vector select."))
                        } else {
                            val future = CompletableFuture<Any?>()
                            previous.thenAccept { vector ->
                                func(frame, vector ?: error("No vector select.")).thenAccept {
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
         * 返回 Vector 对象
         * */
        fun transferNow(func: ScriptFrame.(vector: Vector?) -> Vector): Handler {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Vector?>): CompletableFuture<out Vector> {
                    return if (previous.isDone) {
                        CompletableFuture.completedFuture(func(frame, previous.getNow(null)))
                    } else {
                        previous.thenApply { func(frame, it) }
                    }
                }
            }
        }

        /**
         * 返回 Vector 对象
         * */
        fun transferFuture(func: ScriptFrame.(vector: Vector?) -> CompletableFuture<Vector>): Handler {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Vector?>): CompletableFuture<out Vector> {
                    return if (previous.isDone) {
                        func(frame, previous.getNow(null))
                    } else {
                        val future = CompletableFuture<Vector>()
                        previous.thenAccept { vector ->
                            func(frame, vector).thenAccept {
                                future.complete(it)
                            }
                        }
                        return future
                    }
                }
            }
        }

        /**
         * 接收 Vector 并返回 Vector 对象
         * */
        fun acceptTransferNow(source: LiveData<Vector>?, reproduced: Boolean, func: ScriptFrame.(vector: Vector) -> Vector): Transfer {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Vector?>): CompletableFuture<out Vector> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            val vector = wait.getNow(null)?.let { if (reproduced) it.clone() else it }
                            CompletableFuture.completedFuture(
                                func(frame, vector ?: error("No vector select."))
                            )
                        } else {
                            wait.thenApply {
                                val vector = if (reproduced) it?.clone() else it
                                func(frame, vector ?: error("No vector select."))
                            }
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            val vector = previous.getNow(null)?.let { if (reproduced) it.clone() else it }
                            CompletableFuture.completedFuture(
                                func(frame, vector ?: error("No vector select."))
                            )
                        } else {
                            previous.thenApply {
                                val vector = if (reproduced) it?.clone() else it
                                func(frame, vector ?: error("No vector select."))
                            }
                        }
                    }
                }
            }
        }

        /**
         * 接收 Vector 并返回 Vector 对象
         * */
        fun acceptTransferFuture(source: LiveData<Vector>?, reproduced: Boolean, func: ScriptFrame.(vector: Vector) -> CompletableFuture<Vector>): Transfer {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Vector?>): CompletableFuture<out Vector> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            func(frame, wait.getNow(null) ?: error("No vector select."))
                        } else {
                            val future = CompletableFuture<Vector>()
                            wait.thenAccept { vector ->
                                func(frame, vector ?: error("No vector select.")).thenAccept {
                                    future.complete(it)
                                }
                            }
                            future
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            func(frame, previous.getNow(null) ?: error("No vector select."))
                        } else {
                            val future = CompletableFuture<Vector>()
                            previous.thenAccept { vector ->
                                func(frame, vector ?: error("No vector select.")).thenAccept {
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