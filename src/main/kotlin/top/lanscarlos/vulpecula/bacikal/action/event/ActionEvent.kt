package top.lanscarlos.vulpecula.bacikal.action.event

import org.bukkit.event.Event
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptActionParser
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.internal.ClassInjector
import top.lanscarlos.vulpecula.utils.getVariable
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextPeek
import top.lanscarlos.vulpecula.utils.setVariable
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.event
 *
 * @author Lanscarlos
 * @since 2023-03-23 15:57
 */
class ActionEvent : QuestAction<Any?>() {

    val handlers = mutableListOf<Handler<*>>()

    fun resolve(reader: QuestReader): QuestAction<Any?> {
        do {
            val next = reader.nextToken()
            val isRoot = handlers.isEmpty()
            handlers += registry[next]?.resolve(Reader(next, reader, isRoot))
                ?: error("Unknown sub action \"$next\" at event action.")

            // 判断管道是否已关闭
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
        if (handlers.size == 1 && handlers[0] !is Transfer) {
            return handlers[0].accept(frame).thenApply { it }
        }

        var previous: CompletableFuture<Event> = (handlers[0] as Transfer).accept(frame)

        for (index in 1 until handlers.size - 1) {
            val current = handlers[index]

            // 除去最后一个 Handler 以及非 Transfer
            if (current !is Transfer) break

            // 判断 future 是否已完成，减少嵌套
            previous = if (previous.isDone) {
                val event = previous.getNow(null)
                frame.setVariable("@Transfer", event, false)
                current.accept(frame)
            } else {
                previous.thenCompose { event ->
                    frame.setVariable("@Transfer", event, false)
                    current.accept(frame)
                }
            }
        }

        // 判断 future 是否已完成，减少嵌套
        return if (previous.isDone) {
            val event = previous.getNow(null)
            frame.setVariable("@Transfer", event, false)
            handlers.last().accept(frame).thenApply { it }
        } else {
            previous.thenCompose { event ->
                frame.setVariable("@Transfer", event, false)
                handlers.last().accept(frame).thenApply { it }
            }
        }
    }

    /*
    * 自动注册包下所有解析器 Resolver
    * */
    @Awake(LifeCycle.LOAD)
    companion object : ClassInjector() {

        private val registry = mutableMapOf<String, Resolver>()


        /**
         * 向 Event 语句注册子语句
         * @param resolver 子语句解析器
         * */
        fun registerResolver(resolver: Resolver) {
            resolver.name.forEach { registry[it] = resolver }
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
            id = "event",
            name = ["event"]
        )
        fun parser() = ScriptActionParser<Any?> {
            ActionEvent().resolve(this)
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

        fun transfer(func: Reader.() -> Bacikal.Parser<Event>): Handler<Event> {
            return Transfer(func(this))
        }

        fun source(): LiveData<Event> {
            return if (isRoot) {
                LiveData.frameOf { frame ->
                    frame.getVariable<Event>("@Event")
                        ?: error("No event source selected. [ERROR: event@$token]")
                }
            } else {
                LiveData {
                    Bacikal.Action { frame ->
                        CompletableFuture.completedFuture(
                            frame.getVariable<Event>("@Transfer")
                                ?: error("No event source selected. [ERROR: event@$token]")
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
     * 用于传递 Event
     * */
    open class Transfer(parser: Bacikal.Parser<Event>) : Handler<Event>(parser)
}