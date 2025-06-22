package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.pattern

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptActionParser
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.ActionCanvas
import top.lanscarlos.vulpecula.legacy.internal.ClassInjector
import top.lanscarlos.vulpecula.legacy.bacikal.Bacikal
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalParser
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalReader
import top.lanscarlos.vulpecula.legacy.bacikal.applicative
import top.lanscarlos.vulpecula.legacy.utils.getVariable
import top.lanscarlos.vulpecula.legacy.utils.hasNextToken
import top.lanscarlos.vulpecula.legacy.utils.setVariable
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2022-11-10 11:14
 */
class ActionPattern : QuestAction<Any?>() {

    lateinit var header: Handler<CanvasPattern>
    val handlers = mutableListOf<Handler<Transformer>>()

    fun resolve(reader: QuestReader): QuestAction<Any?> {
        do {
            val next = reader.nextToken()
            val resolver = registry[next.lowercase()] ?: error("Unknown sub action \"$next\" at pattern action.")

            if (::header.isInitialized) {
                handlers += (resolver as? TransformResolver)?.resolve(Reader(next, reader))
                    ?: error("Illegal transformer \"$next\" at pattern action.")
            } else {
                header = (resolver as? PatternResolver)?.resolve(Reader(next, reader))
                    ?: error("Illegal header \"$next\" at pattern action.")
            }
        } while (reader.hasNextToken(">>"))

        return this
    }

    override fun process(frame: ScriptFrame): CompletableFuture<Any?> {

        if (handlers.isEmpty()) {
            if (!::header.isInitialized) {
                error("Illegal pattern action.")
            }

            if (header is Selector) {
                // 无意义
                return header.accept(frame).thenApply { it }
            }

            return header.accept(frame).thenApply { pattern ->
                // 将图案存入图案列表
                val patterns =
                    frame.getVariable<MutableList<CanvasPattern>>(ActionCanvas.VARIABLE_PATTERNS) ?: mutableListOf()
                patterns += pattern
                frame.setVariable(ActionCanvas.VARIABLE_PATTERNS, patterns, deep = false)
                pattern
            }
        }

        return header.accept(frame).thenCompose { pattern ->

            // 包装图案对象
            val decorator = if (pattern !is PatternTransformation) {
                PatternTransformation(pattern)
            } else {
                pattern
            }

            if (header !is Selector) {
                // 非选择器，将图案加入图案列表
                val patterns =
                    frame.getVariable<MutableList<CanvasPattern>>(ActionCanvas.VARIABLE_PATTERNS) ?: mutableListOf()
                patterns += decorator
                frame.setVariable(ActionCanvas.VARIABLE_PATTERNS, patterns, deep = false)
            }

            // 将图案对象存入变量用于传递
            frame.variables().set("@Transfer", decorator)

            // 加载变换器
            return@thenCompose applicative(handlers.map { it.accept(frame) }).thenApply {
                // 加入变换器
                decorator.transformers += it

                // 清除传递变量
                frame.variables().remove("@Transfer")

                // 最后返回图案对象
                decorator
            }
        }
    }

    /*
    * 自动注册包下所有解析器 Resolver
    * */
    @Awake(LifeCycle.LOAD)
    companion object : ClassInjector() {

        private val registry = mutableMapOf<String, Resolver<*>>()

        /**
         * 向 Pattern 语句注册子语句
         * @param resolver 子语句解析器
         * */
        fun registerResolver(resolver: Resolver<*>) {
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
            } as? Resolver<*> ?: return

            registerResolver(resolver)
        }

        @BacikalParser(
            id = "pattern",
            aliases = ["pattern"],
            namespace = "vulpecula-canvas"
        )
        fun parser() = ScriptActionParser<Any?> {
            ActionPattern().resolve(this)
        }
    }

    /**
     * 语句解析器
     * */
    interface Resolver<T : Any> {

        val name: Array<String>

        fun resolve(reader: Reader): Handler<T>
    }

    interface PatternResolver : Resolver<CanvasPattern>

    interface TransformResolver : Resolver<Transformer>

    /**
     * 语句读取器
     * */
    class Reader(val token: String, source: QuestReader) : BacikalReader(source) {

        fun <T : Any> handle(func: Reader.() -> Bacikal.Parser<T>): Handler<T> {
            return Handler(func(this))
        }
    }

    /**
     * 语句处理器
     * */
    open class Handler<T : Any>(val parser: Bacikal.Parser<T>) {

        /**
         * 运行
         * */
        open fun accept(frame: ScriptFrame): CompletableFuture<T> {
            return parser.action.run(frame)
        }
    }

    /**
     * 用于选择 Pattern
     * */
    open class Selector(val index: Int, parser: Bacikal.Parser<CanvasPattern>) : Handler<CanvasPattern>(parser)

}