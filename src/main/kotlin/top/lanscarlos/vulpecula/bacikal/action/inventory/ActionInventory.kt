package top.lanscarlos.vulpecula.bacikal.action.inventory

import org.bukkit.block.BlockState
import org.bukkit.inventory.Inventory
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
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.inventory
 *
 * @author Lanscarlos
 * @since 2023-03-26 14:31
 */
class ActionInventory : QuestAction<Any?>() {

    val handlers = mutableListOf<Handler<*>>()

    fun resolve(reader: QuestReader): QuestAction<Any?> {
        do {
            val next = reader.nextToken()
            handlers += registry[next.lowercase()]?.resolve(Reader(next, reader))
                ?: error("Unknown sub action \"$next\" at inventory action.")

        } while (reader.hasNextToken(">>"))

        return this
    }

    override fun process(frame: ScriptFrame): CompletableFuture<Any?> {
        if (handlers.size == 1) {
            return handlers[0].accept(frame).thenApply { it }
        }

        var previous = handlers[0].accept(frame)

        for (index in 1 until  handlers.size) {
            val current = handlers[index]

            previous = if (previous.isDone) {
                current.accept(frame)
            } else {
                previous.thenCompose { _ ->
                    current.accept(frame)
                }
            }
        }

        return previous.thenApply { it }
    }

    /*
    * 自动注册包下所有解析器 Resolver
    * */
    @Awake(LifeCycle.LOAD)
    companion object : ClassInjector() {

        private val registry = mutableMapOf<String, Resolver>()

        /**
         * 向 Inventory 语句注册子语句
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
            id = "inventory",
            name = ["v-inv", "inventory*", "inv*"],
            override = ["inventory", "inv"]
        )
        fun parser() = ScriptActionParser<Any?> {
            ActionInventory().resolve(this)
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
    class Reader(val token: String, source: QuestReader) : BacikalReader(source) {

        fun <T> handle(func: Reader.() -> Bacikal.Parser<T>): Handler<T> {
            return Handler(func(this))
        }

        fun source(): LiveData<Inventory> {
            return LiveData {
                Bacikal.Action { frame ->
                    val inventory =
                        frame.getVariable<Inventory>("@Inventory") ?: frame.playerOrNull()?.toBukkit()?.inventory
                        ?: error("No inventory source selected. [ERROR: item@$token]")
                    CompletableFuture.completedFuture(inventory)
                }
            }
        }

        /**
         * 更新容器，用于更新方块类容器
         * */
        fun ScriptFrame.updateInventory() {
            this.getVariable<BlockState>("@InventoryHolder")?.update(true)
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
}