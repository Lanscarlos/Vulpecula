package top.lanscarlos.vulpecula.kether.action

import net.luckperms.api.LuckPerms
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.MetaNode
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.*
import java.util.*
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
    val unique: ParsedAction<*>?,
    val storage: String?
) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        return listOf(
            key.getOrNull(frame),
            value?.let { frame.run(it) },
            unique?.let { frame.run(it) }
        ).thenTake().thenApply { args ->
            val key = args[0]?.toString() ?: error("No key selected.")
            val value = args[1]
            val unique = args[2]

            if (value != null) {
                // 设置变量
                setMemory(key, value, unique, storage)
            } else {
                getMemory(key, unique, storage)
            }
        }
    }

    companion object {

        val luckPermsAPI by lazy {
            try {
                Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider
            } catch (e: Exception) {
                null
            }
        }

        private val cache = ConcurrentHashMap<Entity, ConcurrentHashMap<String, Any>>()
        private val globalCache = ConcurrentHashMap<String, Any>()

        /**
         * 获取变量
         *
         * @param unique 对应实体，若为 null 则为全局变量
         * @param storage 存储容器，默认使用 cache 存储
         * */
        fun getMemory(key: String, unique: Any?, storage: String? = null): Any? {
            when (storage?.lowercase()) {
                "luckperms", "lp" -> {
                    val api = luckPermsAPI ?: error("No LuckPerms plugin service found.")
                    val user = when (unique) {
                        is Player -> api.getPlayerAdapter(Player::class.java).getUser(unique)
                        else -> {
                            Bukkit.getPlayerExact(unique.toString())?.let {
                                api.getPlayerAdapter(Player::class.java).getUser(it)
                            }
                        }
                    } ?: error("No LuckPerms user service found.")

                    return user.cachedData.metaData.getMetaValue(key)
                }
                else -> {
                    val map = if (unique is Entity) {
                        cache.computeIfAbsent(unique) { ConcurrentHashMap() }
                    } else {
                        globalCache
                    }

                    return map[key]
                }
            }
        }

        /**
         * 获取变量
         *
         * @param value 若为空则删除对应键值
         * @param unique 对应实体，若为 null 则为全局变量
         * @param storage 存储容器，默认使用 cache 存储
         *
         * @return value 的值
         * */
        fun setMemory(key: String, value: Any?, unique: Any?, storage: String? = null): Any? {
            when (storage?.lowercase()) {
                "luckperms", "lp" -> {
                    val api = luckPermsAPI ?: error("No LuckPerms plugin service found.")
                    val user = when (unique) {
                        is Player -> api.getPlayerAdapter(Player::class.java).getUser(unique)
                        else -> {
                            Bukkit.getPlayerExact(unique.toString())?.let {
                                api.getPlayerAdapter(Player::class.java).getUser(it)
                            }
                        }
                    } ?: error("No LuckPerms user service found.")

                    user.data().clear(NodeType.META.predicate { it.metaKey == key })

                    if (value != null) {
                        val node = MetaNode.builder(key, value.toString()).build()
                        user.data().add(node)
                        api.userManager.saveUser(user)
                    }
                }
                else -> {
                    val map = if (unique is Entity) {
                        cache.computeIfAbsent(unique) { ConcurrentHashMap() }
                    } else {
                        globalCache
                    }

                    if (value != null) {
                        map[key] = value
                    } else {
                        map.remove(key)
                    }
                }
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
         * memory {key} by &entity using lp
         * memory {key} -global(g)
         *
         * 设置变量
         * memory {key} to {value}
         * memory {key} to {value} by &entity
         * memory {key} to {value} by &entity using lp
         * memory {key} to {value} -global(g)
         * */
        @VulKetherParser(
            id = "memory",
            name = ["memory"]
        )
        fun parser() = scriptParser { reader ->
            val key = reader.readString()

            val value = if (reader.hasNextToken("to")) {
                reader.nextBlock()
            } else null

            val entity = if (reader.hasNextToken("by", "with")) {
                reader.nextParsedAction()
            } else null

            val storage = reader.tryNextToken("using")

            // 全局变量不以
            ActionMemory(key, value, entity, storage)
        }
    }
}