package top.lanscarlos.vulpecula.kether.action.location

import taboolib.common.platform.function.platformLocation
import taboolib.common.util.Location
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.internal.ClassInjector
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.DoubleLiveData
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.location
 *
 * @author Lanscarlos
 * @since 2022-11-25 23:05
 */
class ActionLocation : ScriptAction<Any?>() {

    private val handlers = mutableListOf<Handler>()

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        var previous: CompletableFuture<out Location?> = CompletableFuture.completedFuture(null)
        for (handler in handlers) {
            if (handler is Transfer) {
                previous = handler.handle(frame, previous)
            } else {
                return handler.handle(frame, previous).thenApply {
                    if (it is Location) platformLocation<Any>(it) else it
                }
            }
        }
        return previous.thenApply { if (it != null) platformLocation<Any>(it) else it }
    }


    companion object : ClassInjector(ActionLocation::class.java.packageName) {

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
            id = "location",
            name = ["loc*", "location*"],
            override = ["loc", "location"]
        )
        fun parser() = scriptParser { reader ->
            val action = ActionLocation()
            do {
                reader.mark()
                val it = reader.nextToken()
                val isRoot = action.handlers.isEmpty()

                action.handlers += registry[it]?.read(reader, it, isRoot) ?: let {
                    // 兼容 TabooLib 原生 location 语句的构建坐标功能
                    reader.reset()
                    LocationBuildHandler.readLegacy(reader)
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
        fun handle(frame: ScriptFrame, previous: CompletableFuture<out Location?>): CompletableFuture<out Any?>
    }

    /**
     * 处理后返回 Location 对象，供下一处理器使用
     * */
    interface Transfer : Handler {
        override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Location?>): CompletableFuture<out Location>
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

        fun isReproduced(reader: QuestReader): Boolean {
            return !reader.hasNextToken("not-reproduced", "not-rep", "not-clone", "-n")
        }

        /**
         * 返回任意对象
         * */
        fun handleNow(func: ScriptFrame.(location: Location?) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Location?>): CompletableFuture<out Any?> {
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
        fun handleFuture(func: ScriptFrame.(location: Location?) -> CompletableFuture<Any?>): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Location?>): CompletableFuture<out Any?> {
                    return if (previous.isDone) {
                        func(frame, previous.getNow(null))
                    } else {
                        val future = CompletableFuture<Any?>()
                        previous.thenAccept { location ->
                            func(frame, location).thenAccept {
                                future.complete(it)
                            }
                        }
                        return future
                    }
                }
            }
        }

        /**
         * 接收 Location 返回任意对象
         * */
        fun acceptHandleNow(source: LiveData<Location>?, func: ScriptFrame.(location: Location) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Location?>): CompletableFuture<out Any?> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            CompletableFuture.completedFuture(
                                func(frame, wait.getNow(null) ?: error("No location selected."))
                            )
                        } else {
                            wait.thenApply {
                                func(frame, it ?: error("No location selected."))
                            }
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            CompletableFuture.completedFuture(
                                func(frame, previous.getNow(null) ?: error("No location selected."))
                            )
                        } else {
                            previous.thenApply {
                                func(frame, it ?: error("No location selected."))
                            }
                        }
                    }
                }
            }
        }

        /**
         * 接收 Location 返回任意对象
         * */
        fun acceptHandleFuture(source: LiveData<Location>?, func: ScriptFrame.(location: Location) -> CompletableFuture<Any?>): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Location?>): CompletableFuture<out Any?> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            func(frame, wait.getNow(null) ?: error("No location selected."))
                        } else {
                            val future = CompletableFuture<Any?>()
                            wait.thenAccept { location ->
                                func(frame, location ?: error("No location selected.")).thenAccept {
                                    future.complete(it)
                                }
                            }
                            future
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            func(frame, previous.getNow(null) ?: error("No location selected."))
                        } else {
                            val future = CompletableFuture<Any?>()
                            previous.thenAccept { location ->
                                func(frame, location ?: error("No location selected.")).thenAccept {
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
         * 返回 Location 对象
         * */
        fun transferNow(func: ScriptFrame.(location: Location?) -> Location): Handler {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Location?>): CompletableFuture<out Location> {
                    return if (previous.isDone) {
                        CompletableFuture.completedFuture(func(frame, previous.getNow(null)))
                    } else {
                        previous.thenApply { func(frame, it) }
                    }
                }
            }
        }

        /**
         * 返回 Location 对象
         * */
        fun transferFuture(func: ScriptFrame.(location: Location?) -> CompletableFuture<Location>): Handler {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Location?>): CompletableFuture<out Location> {
                    return if (previous.isDone) {
                        func(frame, previous.getNow(null))
                    } else {
                        val future = CompletableFuture<Location>()
                        previous.thenAccept { location ->
                            func(frame, location).thenAccept {
                                future.complete(it)
                            }
                        }
                        return future
                    }
                }
            }
        }

        /**
         * 接收 Location 并返回 Location 对象
         * */
        fun acceptTransferNow(source: LiveData<Location>?, reproduced: Boolean, func: ScriptFrame.(location: Location) -> Location): Transfer {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Location?>): CompletableFuture<out Location> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            val location = wait.getNow(null) ?: error("No location selected.")
                            CompletableFuture.completedFuture(
                                func(frame, if (reproduced) location.clone() else location)
                            )
                        } else {
                            wait.thenApply {
                                val location = if (reproduced) it?.clone() else it
                                func(frame, location ?: error("No location selected."))
                            }
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            val location = previous.getNow(null) ?: error("No location selected.")
                            CompletableFuture.completedFuture(
                                func(frame, if (reproduced) location.clone() else location)
                            )
                        } else {
                            previous.thenApply {
                                val location = if (reproduced) it?.clone() else it
                                func(frame, location ?: error("No location selected."))
                            }
                        }
                    }
                }
            }
        }

        /**
         * 接收 Location 并返回 Location 对象
         * */
        fun acceptTransferFuture(source: LiveData<Location>?, reproduced: Boolean, func: ScriptFrame.(location: Location) -> CompletableFuture<Location>): Transfer {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: CompletableFuture<out Location?>): CompletableFuture<out Location> {
                    return if (source != null) {
                        // root
                        val wait = source.getOrNull(frame)
                        if (wait.isDone) {
                            val location = wait.getNow(null) ?: error("No location selected.")
                            func(frame, if (reproduced) location.clone() else location)
                        } else {
                            val future = CompletableFuture<Location>()
                            wait.thenAccept { location ->
                                func(frame, (if (reproduced) location?.clone() else location) ?: error("No location selected.")).thenAccept {
                                    future.complete(it)
                                }
                            }
                            future
                        }
                    } else {
                        // not root
                        if (previous.isDone) {
                            val location = previous.getNow(null) ?: error("No location selected.")
                            func(frame, if (reproduced) location.clone() else location)
                        } else {
                            val future = CompletableFuture<Location>()
                            previous.thenAccept { location ->
                                func(frame, (if (reproduced) location?.clone() else location) ?: error("No location selected.")).thenAccept {
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