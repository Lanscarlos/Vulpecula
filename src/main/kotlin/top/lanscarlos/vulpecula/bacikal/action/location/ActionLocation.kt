package top.lanscarlos.vulpecula.bacikal.action.location

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.platformLocation
import taboolib.common.util.Location
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptActionParser
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.bacikal.*
import top.lanscarlos.vulpecula.internal.ClassInjector
import top.lanscarlos.vulpecula.utils.getVariable
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextPeek
import top.lanscarlos.vulpecula.utils.setVariable
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.location
 *
 * @author Lanscarlos
 * @since 2023-03-20 16:38
 */
class ActionLocation : QuestAction<Any?>() {

    val handlers = mutableListOf<Handler<*>>()

    fun resolve(reader: QuestReader): QuestAction<Any?> {
        do {
            reader.mark()
            val next = reader.nextToken()
            val isRoot = handlers.isEmpty()
            handlers += registry[next.lowercase()]?.resolve(Reader(next, reader, isRoot))
                ?: let {
                    // 兼容 TabooLib 原生 location 语句的构建坐标功能
                    reader.reset()
                    ActionLocationBuild.resolve(Reader(next, reader, isRoot))
                }

            // 判断管道是否已经关闭
            if (handlers.lastOrNull() !is Transfer) {
                if (reader.hasNextToken(">>")) {
                    error("Cannot use \">> ${reader.nextPeek()}\", previous action \"$next\" has closed the pipeline.")
                }
                break
            }
        } while (reader.hasNextToken(">>"))
        return this
    }

    override fun process(frame: ScriptFrame): CompletableFuture<Any?> {
        if (handlers.size == 1 || handlers[0] !is Transfer) {
            return handlers[0].accept(frame).thenApply { adaptLocation(it) }
        }

        var previous: CompletableFuture<Location> = (handlers[0] as Transfer).accept(frame)

        for (index in 1 until handlers.size - 1) {
            val current = handlers[index]

            // 除去最后一个 Handler 以及非 Transfer
            if (current !is Transfer) break

            // 判断 future 是否已完成，减少嵌套
            previous = if (previous.isDone) {
                val location = previous.getNow(null)
                frame.setVariable("@Transfer", location, false)
                current.accept(frame)
            } else {
                previous.thenCompose { location ->
                    frame.setVariable("@Transfer", location, false)
                    current.accept(frame)
                }
            }
        }

        // 判断 future 是否已完成，减少嵌套
        return if (previous.isDone) {
            val location = previous.getNow(null)
            frame.setVariable("@Transfer", location, false)
            handlers.last().accept(frame).thenApply { adaptLocation(it) }
        } else {
            previous.thenCompose { location ->
                frame.setVariable("@Transfer", location, false)
                handlers.last().accept(frame).thenApply { adaptLocation(it) }
            }
        }
    }

    fun adaptLocation(any: Any?): Any? {
        return if (any is Location) {
            platformLocation(any)
        } else {
            any
        }
    }

    /*
    * 自动注册包下所有解析器 Resolver
    * */
    @Awake(LifeCycle.LOAD)
    companion object : ClassInjector() {

        private val registry = mutableMapOf<String, Resolver>()

        /**
         * 向 Location 语句注册子语句
         * @param resolver 子语句解析器
         * */
        fun registerResolver(resolver: Resolver) {
            resolver.name.forEach { registry[it.lowercase()] = resolver }
        }

        override fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {
            if (!Resolver::class.java.isAssignableFrom(clazz)) return

            val resolver = let {
                if (supplier?.get() != null) {
                    supplier.get()
                } else try {
                    clazz.getDeclaredConstructor().newInstance()
                } catch (e: Exception) {
                    null
                }
            } as? Resolver ?: return

            registerResolver(resolver)
        }

        @BacikalParser(
            id = "location",
            name = ["location*", "loc*"],
            override = ["location", "loc"]
        )
        fun parser() = ScriptActionParser<Any?> {
            ActionLocation().resolve(this)
        }
    }

    /**
     * 语句解析器
     * */
    interface Resolver {

        val name: Array<String>

        fun resolve(reader: Reader): Handler<out Any?>
    }

    /**
     * 语句读取器
     * */
    class Reader(val token: String, source: QuestReader, val isRoot: Boolean) : BacikalReader(source) {

        fun <T> handle(func: Reader.() -> Bacikal.Parser<T>): Handler<T> {
            return Handler(func(this))
        }

        fun transfer(func: Reader.() -> Bacikal.Parser<Location>): Handler<Location> {
            return Transfer(func(this))
        }

        fun source(): LiveData<Location> {
            return if (isRoot) {
                location(display = "location source")
            } else {
                LiveData {
                    Bacikal.Action { frame ->
                        CompletableFuture.completedFuture(
                            frame.getVariable<Location>("@Transfer")
                                ?: error("No location source selected. [ERROR: location@$token]")
                        )
                    }
                }
            }
        }

    }

    /**
     * 语句处理器
     * */
    open class Handler<T : Any?>(val parser: Bacikal.Parser<T>) {

        /**
         * 运行
         * */
        open fun accept(frame: ScriptFrame): CompletableFuture<T> {
            return parser.action.run(frame)
        }
    }

    /**
     * 用于传递 Location
     * */
    open class Transfer(parser: Bacikal.Parser<Location>) : Handler<Location>(parser)
}