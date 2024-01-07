package top.lanscarlos.vulpecula.bacikal.action.target

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
 * top.lanscarlos.vulpecula.bacikal.action.target
 *
 * @author Lanscarlos
 * @since 2023-03-22 20:02
 */
class ActionTarget : QuestAction<Any?>() {

    val handlers = mutableListOf<Handler<*>>()

    fun resolve(reader: QuestReader): QuestAction<Any?> {
        do {
            val next = reader.nextToken().lowercase()
            val isRoot = handlers.isEmpty()

            // 获取解析器
            val resolver = if (next[0] == '@') {
                selectors[next.substring(1)]
            } else {
                reader.mark()
                val input = reader.nextToken().lowercase()
                when (next) {
                    "selector", "select", "sel" -> selectors[input]
                    "filter" -> filters[input]
                    "foreach" -> ActionTargetForeach
                    else -> {
                        reader.reset()
                        ActionTargetForeach
                    }
                }
            }

            handlers += resolver?.resolve(Reader(next, reader, isRoot))
                ?: error("Unknown sub action \"$next\" at target action.")

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
        if (handlers.size == 1 || handlers[0] !is Transfer) {
            return handlers[0].accept(frame).thenApply { adaptTarget(it) }
        }

        var previous: CompletableFuture<MutableCollection<Any>> = (handlers[0] as Transfer).accept(frame)

        for (index in 1 until handlers.size - 1) {
            val current = handlers[index]

            // 除去最后一个 Handler 以及非 Transfer
            if (current !is Transfer) break

            // 判断 future 是否已完成，减少嵌套
            previous = if (previous.isDone) {
                val target = previous.getNow(null)
                frame.setVariable("@Transfer", target, false)
                current.accept(frame)
            } else {
                previous.thenCompose { target ->
                    frame.setVariable("@Transfer", target, false)
                    current.accept(frame)
                }
            }
        }

        // 判断 future 是否已完成，减少嵌套
        return if (previous.isDone) {
            val target = previous.getNow(null)
            frame.setVariable("@Transfer", target, false)
            handlers.last().accept(frame).thenApply { adaptTarget(it) }
        } else {
            previous.thenCompose { target ->
                frame.setVariable("@Transfer", target, false)
                handlers.last().accept(frame).thenApply { adaptTarget(it) }
            }
        }
    }

    fun adaptTarget(any: Any?): Any? {
        return if (any is Collection<*> && any.size == 1) {
            any.single()
        } else {
            any
        }
    }

    /*
    * 自动注册包下所有解析器 Resolver
    * */
    @Awake(LifeCycle.LOAD)
    companion object : ClassInjector() {

        private val selectors = mutableMapOf<String, Resolver>()
        private val filters = mutableMapOf<String, Resolver>()

        /**
         * 向 Target 语句注册子语句
         * @param resolver 子语句解析器
         * */
        fun registerSelector(resolver: Resolver) {
            resolver.name.forEach { selectors[it.lowercase()] = resolver }
        }

        /**
         * 向 Target 语句注册子语句
         * @param resolver 子语句解析器
         * */
        fun registerFilter(resolver: Resolver) {
            resolver.name.forEach { filters[it.lowercase()] = resolver }
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

            // 判断解析器类型
            when (resolver::class.java.`package`.name.substringAfterLast('.')) {
                "selector" -> registerSelector(resolver)
                "filter" -> registerFilter(resolver)
            }
        }

        @BacikalParser("target")
        fun parser() = ScriptActionParser<Any?> {
            ActionTarget().resolve(this)
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

        fun transfer(func: Reader.() -> Bacikal.Parser<MutableCollection<Any>>): Handler<MutableCollection<Any>> {
            return Transfer(func(this))
        }

        fun source(): LiveData<MutableCollection<Any>> {
            return if (isRoot) {
                if (token == "filter" || token == "foreach") {
                    // 以 filter 和 foreach 开头
                    LiveData.frameBy {
                        when (it) {
                            is Array<*> -> it.filterNotNull().toMutableSet()
                            is Collection<*> -> it.filterNotNull().toMutableSet()
                            else -> if (it != null) mutableSetOf(it) else mutableSetOf()
                        }
                    }
                } else {
                    // 以 selector 开头
                    LiveData.point(mutableSetOf())
                }
            } else {
                LiveData {
                    Bacikal.Action { frame ->
                        CompletableFuture.completedFuture(
                            frame.getVariable<MutableCollection<Any>>("@Transfer")
                                ?: error("No target source selected. [ERROR: target@$token]")
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
     * 用于传递 Target
     * */
    open class Transfer(parser: Bacikal.Parser<MutableCollection<Any>>) : Handler<MutableCollection<Any>>(parser)

}