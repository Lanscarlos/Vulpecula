package top.lanscarlos.vulpecula.kether.action

import org.bukkit.entity.Entity
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.readEntity
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-12 00:23
 */
class ActionMemory(
    val key: LiveData<String>,
    val value: ParsedAction<*>?,
    val entity: LiveData<Entity>?
) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        val defKey = "UNKNOWN_KEY"
        return if (this.value != null) {
            // 设置变量

            if (entity != null) {
                // 全局
                key.thenApply(frame, defKey, frame.run(value), entity.getOrNull(frame)) {
                    setMemory(this, it.first(), it.last() as? Entity)
                }
            } else {
                // 非全局
                key.thenApply(frame, defKey, frame.run(value)) {
                    setMemory(this, it.first(), null)
                }
            }
        } else {
            // 获取变量

            if (entity != null) {
                // 全局
                key.thenApply(frame, defKey, entity.getOrNull(frame)) {
                    setMemory(this, it.first(), it.last() as? Entity)
                }
            } else {
                // 非全局
                key.get(frame, defKey).thenApply {
                    getMemory(it, null)
                }
            }
        }
    }

    companion object {

        private val cache = ConcurrentHashMap<Entity, ConcurrentHashMap<String, Any>>()
        private val globalCache = ConcurrentHashMap<String, Any>()

        /**
         * 获取变量
         *
         * @param entity 对应实体，若为 null 则为全局变量
         * */
        fun getMemory(key: String, entity: Entity?): Any? {
            val map = if (entity != null) {
                cache.computeIfAbsent(entity) { ConcurrentHashMap() }
            } else {
                globalCache
            }
            return map[key]
        }

        /**
         * 获取变量
         *
         * @param value 若为空则删除对应键值
         * @param entity 对应实体，若为 null 则为全局变量
         *
         * @return value 的值
         * */
        fun setMemory(key: String, value: Any?, entity: Entity?): Any? {
            val map = if (entity != null) {
                cache.computeIfAbsent(entity) { ConcurrentHashMap() }
            } else {
                globalCache
            }
            if (value != null) {
                map[key] = value
            } else {
                map.remove(key)
            }
            return value
        }

        /**
         *
         * 变量是默认相对于实体独立的
         *
         * 获取变量
         * memory {key}
         * memory {key} by &entity
         * memory {key} -global(g)
         *
         * 设置变量
         * memory {key} to {value}
         * memory {key} to {value} by &entity
         * memory {key} to {value} -global(g)
         * */
        @VulKetherParser(
            id = "memory",
            name = ["memory"]
        )
        fun parser() = scriptParser { reader ->
            val key = StringLiveData(reader.nextBlock())
            val value = if (reader.hasNextToken("to")) {
                reader.nextBlock()
            } else null

            val entity = if (reader.hasNextToken("by")) {
                reader.readEntity()
            } else null

            // 全局变量不以
            ActionMemory(key, value, entity)
        }
    }
}