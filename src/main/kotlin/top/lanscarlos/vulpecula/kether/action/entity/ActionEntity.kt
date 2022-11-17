package top.lanscarlos.vulpecula.kether.action.entity

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.internal.ClassInjector
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextPeek
import top.lanscarlos.vulpecula.utils.readEntity
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.entity
 *
 * @author Lanscarlos
 * @since 2022-11-17 23:01
 */
class ActionEntity : ScriptAction<Any?>() {

    private val handlers = mutableListOf<Handler>()

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        var previous: Entity? = null
        for (handler in handlers) {
            if (handler is Transfer) {
                previous = handler.handle(frame, previous)
            } else {
                return CompletableFuture.completedFuture(
                    handler.handle(frame, previous)
                )
            }
        }
        return CompletableFuture.completedFuture(previous)
    }

    companion object : ClassInjector(packageName = ActionEntity::class.java.packageName) {

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
            id = "entity",
            name = ["entity"]
        )
        fun parser() = scriptParser { reader ->
            val action = ActionEntity()
            do {
                val it = reader.nextToken()
                val isRoot = action.handlers.isEmpty()

                action.handlers += registry[it]?.read(reader, it, isRoot) ?: error("Unknown argument \"$it\" at entity action.")

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
        fun handle(frame: ScriptFrame, previous: Entity?): Any?
    }

    /**
     * 处理后返回 Entity 对象，供下一处理器使用
     * */
    interface Transfer : Handler {
        override fun handle(frame: ScriptFrame, previous: Entity?): Entity
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

        fun QuestReader.source(isRoot: Boolean): LiveData<Entity>? {
            return if (isRoot) this.readEntity() else null
        }

        /**
         * 返回任意对象
         * */
        fun handle(func: ScriptFrame.(entity: Entity?) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: Entity?): Any? {
                    return func(frame, previous)
                }
            }
        }

        /**
         * 接收 Entity 返回任意对象
         * */
        fun acceptEntity(source: LiveData<Entity>?, func: ScriptFrame.(entity: Entity) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: Entity?): Any? {
                    val entity = previous ?: source?.getOrNull(frame) ?: error("No entity select.")
                    return func(frame, entity)
                }
            }
        }

        /**
         * 接收 LivingEntity 返回任意对象
         * */
        fun acceptLivingEntity(source: LiveData<Entity>?, func: ScriptFrame.(entity: LivingEntity) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: Entity?): Any? {
                    val entity = previous ?: source?.getOrNull(frame) ?: error("No entity select.")
                    return func(frame, entity as? LivingEntity ?: return entity)
                }
            }
        }

        /**
         * 接收 Player 返回任意对象
         * */
        fun acceptPlayer(source: LiveData<Entity>?, func: ScriptFrame.(player: Player) -> Any?): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: Entity?): Any? {
                    val entity = previous ?: source?.getOrNull(frame) ?: error("No entity select.")
                    return func(frame, entity as? Player ?: return entity)
                }
            }
        }

        /**
         * 返回 Entity 对象
         * */
        fun transfer(func: ScriptFrame.(entity: Entity?) -> Entity): Handler {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: Entity?): Entity {
                    return func(frame, previous)
                }
            }
        }

        /**
         * 接收 Entity 并返回 Entity 对象
         * */
        fun applyEntity(source: LiveData<Entity>?, func: ScriptFrame.(entity: Entity) -> Entity): Transfer {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: Entity?): Entity {
                    val entity = previous ?: source?.getOrNull(frame) ?: error("No entity select.")
                    return func(frame, entity)
                }
            }
        }

        /**
         * 接收 LivingEntity 并返回 LivingEntity 对象
         * */
        fun applyLivingEntity(source: LiveData<Entity>?, func: ScriptFrame.(entity: LivingEntity) -> LivingEntity): Transfer {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: Entity?): Entity {
                    val entity = previous ?: source?.getOrNull(frame) ?: error("No entity select.")
                    return func(frame, entity as? LivingEntity ?: return entity)
                }
            }
        }

        /**
         * 接收 Entity 并返回 Entity 对象
         * */
        fun applyPlayer(source: LiveData<Entity>?, func: ScriptFrame.(player: Player) -> Player): Transfer {
            return object : Transfer {
                override fun handle(frame: ScriptFrame, previous: Entity?): Entity {
                    val entity = previous ?: source?.getOrNull(frame) ?: error("No entity select.")
                    return func(frame, entity as? Player ?: return entity)
                }
            }
        }
    }
}