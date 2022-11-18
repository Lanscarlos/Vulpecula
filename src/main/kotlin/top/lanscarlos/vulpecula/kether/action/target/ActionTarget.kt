package top.lanscarlos.vulpecula.kether.action.target

import org.bukkit.entity.Entity
import taboolib.common.platform.function.info
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.internal.ClassInjector
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextPeek
import top.lanscarlos.vulpecula.utils.readCollection
import top.lanscarlos.vulpecula.utils.readEntity
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
        var collection: MutableCollection<Any> = mutableSetOf()
        for (handler in handlers) {
            collection = handler.handle(frame, collection)
        }

        return if (collection.size == 1) {
            CompletableFuture.completedFuture(collection.single())
        } else {
            CompletableFuture.completedFuture(collection)
        }
    }

    companion object : ClassInjector(packageName = ActionTarget::class.java.packageName) {

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
            val type = clazz.packageName.split('.').lastOrNull() ?: return
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
        fun handle(frame: ScriptFrame, collection: MutableCollection<Any>): MutableCollection<Any>
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
        fun handle(func: ScriptFrame.(collection: MutableCollection<Any>) -> MutableCollection<Any>): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, collection: MutableCollection<Any>): MutableCollection<Any> {
                    return func(frame, collection)
                }
            }
        }

        /**
         * 接收 Collection 并返回 Collection 对象
         * */
        fun acceptHandler(source: LiveData<Collection<*>>?, func: ScriptFrame.(collection: MutableCollection<Any>) -> MutableCollection<Any>): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, collection: MutableCollection<Any>): MutableCollection<Any> {
                    val collections = source?.getOrNull(frame)?.mapNotNull { it }?.toMutableSet() ?: collection
                    return func(frame, collections)
                }
            }
        }
    }
}