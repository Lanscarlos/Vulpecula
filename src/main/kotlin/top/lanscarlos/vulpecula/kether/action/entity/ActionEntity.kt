package top.lanscarlos.vulpecula.kether.action.entity

import org.bukkit.entity.Entity
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.kether.Parser
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.internal.ClassInjector
import top.lanscarlos.vulpecula.kether.ParserBuilder
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.entity
 *
 * @author Lanscarlos
 * @since 2022-11-17 23:01
 */
class ActionEntity : QuestAction<Any?>() {

    val handlers = mutableListOf<Handler<*>>()

    @Suppress("UNCHECKED_CAST")
    fun resolve(reader: QuestReader): QuestAction<Any?> {
        do {
            val next = reader.nextToken()
            val isRoot = handlers.isEmpty()
            handlers += registry[next]?.resolve(Reader(next, reader, isRoot)) ?: error("Unknown sub action \"$next\" at entity action.")

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
        var previous: CompletableFuture<out Entity?> = CompletableFuture.completedFuture(null)

        for ((index, handler) in handlers.withIndex()) {
            // 除去最后一个 Handler 以及非 Transfer
            if (handler !is Transfer || index == handlers.lastIndex) break
            previous = previous.thenCompose { entity ->
                frame.setVariable("@Entity", entity, false)
                frame.setVariable("entity", entity, false)
                handler.process(frame)
            }
        }

        return previous.thenCompose { entity ->
            frame.setVariable("@Entity", entity, false)
            frame.setVariable("entity", entity, false)
            handlers.last().process(frame).thenApply { it }
        }
    }

    /*
    * 自动注册包下所有解析器 Resolver
    * */
    @Awake(LifeCycle.LOAD)
    companion object : ClassInjector() {

        private val registry = mutableMapOf<String, Resolver>()

        /**
         * 向 Entity 语句注册子语句
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

        @VulKetherParser(
            id = "entity",
            name = ["entity"]
        )
        fun parser() = scriptParser {
            ActionEntity().resolve(it)
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
    class Reader(val token: String, reader: QuestReader, val isRoot: Boolean) : QuestReader by reader, ParserBuilder {

        fun <T> handle(func: Reader.() -> Parser<Parser.Action<T>>): Handler<T> {
            return Handler(func(this)).resolve(this)
        }

        fun transfer(func: Reader.() -> Parser<Parser.Action<Entity>>): Handler<Entity> {
            return Transfer(func(this)).resolve(this)
        }

        fun source() : Parser<Entity> {
            return if (isRoot) {
                entity()
            } else {
                Parser.frame {
                    now { this.getVariable<Entity>("@Entity", "entity") ?: error("No entity selected. [ERROR: entity@$token]") }
                }
            }
        }

    }

    /**
     * 语句处理器
     * */
    open class Handler<T: Any?>(val parser: Parser<Parser.Action<T>>) {

        /**
         * 待运行的解析结果
         * */
        lateinit var action: Parser.Action<Parser.Action<T>>

        /**
         * 解析
         * */
        open fun resolve(reader: QuestReader): Handler<T> {
            action = parser.reader.apply(reader)
            return this
        }

        /**
         * 运行
         * */
        open fun process(frame: ScriptFrame): CompletableFuture<T> {
            return action.run(frame).thenCompose {
                it.run(frame)
            }
        }
    }

    /**
     * 用于传递 Entity
     * */
    open class Transfer(parser: Parser<Parser.Action<Entity>>): Handler<Entity>(parser)
}