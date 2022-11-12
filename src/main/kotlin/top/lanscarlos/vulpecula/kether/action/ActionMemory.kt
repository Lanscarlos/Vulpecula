package top.lanscarlos.vulpecula.kether.action

import org.bukkit.entity.Entity
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.module.kether.scriptParser
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.unsafePlayer
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
    val entity: ParsedAction<*>?,
    val global: Boolean
) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        val key = this.key.get(frame, "UNKNOWN_KEY")
        val entity = if (!global) {
            this.entity?.let {
                frame.run(it).join() as? Entity
            } ?: (frame.unsafePlayer() as? BukkitPlayer)?.player
        } else null

        return if (this.value != null) {
            // 设置变量
            val value = frame.run(this.value).join()
            setMemory(key, value, entity)
            CompletableFuture.completedFuture(value)
        } else {
            // 获取变量
            val value = getMemory(key, entity)
            CompletableFuture.completedFuture(value)
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
                reader.nextBlock()
            } else null

            val global = reader.hasNextToken("-global", "global", "-g")

            ActionMemory(key, value, entity, global)
        }
    }
}