package top.lanscarlos.vulpecula.legacy.bacikal.action

import net.luckperms.api.LuckPerms
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.MetaNode
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalParser
import top.lanscarlos.vulpecula.legacy.bacikal.bacikal
import top.lanscarlos.vulpecula.legacy.utils.playerOrNull
import top.lanscarlos.vulpecula.legacy.utils.toBukkit
import top.maplex.abolethcore.AbolethUtils
import java.util.concurrent.ConcurrentHashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action
 *
 * @author Lanscarlos
 * @since 2023-03-26 00:30
 */
object ActionMemory {

    @BacikalParser(
        id = "memory",
        aliases = ["memory"]
    )
    fun parser() = bacikal {
        combine(
            text("key"),
            optional("to", then = any(), def = "@GET"),
            optional("by", "with", then = any()),
            optional("using", then = text("default"), def = "default")
        ) { key, value, unique, storage ->
            when (value) {
                "@GET" -> {
                    // 获取变量
                    getMemory(this, key, unique, storage)
                }
                "@REMOVE", "@DELETE" -> {
                    // 删除变量
                    setMemory(this, key, null, unique, storage)
                }
                else -> {
                    // 设置变量
                    setMemory(this, key, value, unique, storage)
                }
            }
        }
    }

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
    fun getMemory(frame: ScriptFrame, key: String, unique: Any?, storage: String? = null): Any? {
        when (storage?.lowercase()) {
            "luckperms", "lp" -> {
                val api = luckPermsAPI ?: error("No LuckPerms plugin service found.")
                val user = when (unique) {
                    is Player -> api.getPlayerAdapter(Player::class.java).getUser(unique)
                    else -> {
                        val player = Bukkit.getPlayerExact(unique.toString()) ?: frame.playerOrNull()?.toBukkit()
                        api.getPlayerAdapter(Player::class.java).getUser(player ?: error("No LuckPerms player selected."))
                    }
                }

                return user.cachedData.metaData.getMetaValue(key)
            }
            "aboleth", "abo" -> {
                val uuid = when (unique) {
                    is Player -> unique.uniqueId
                    is ProxyPlayer -> unique.uniqueId
                    is String -> Bukkit.getPlayerExact(unique)?.uniqueId
                    else -> null
                }

                return AbolethUtils.get(uuid ?: AbolethUtils.getServerUUID(), key)
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
    fun setMemory(frame: ScriptFrame, key: String, value: Any?, unique: Any?, storage: String? = null): Any? {
        when (storage?.lowercase()) {
            "luckperms", "lp" -> {
                val api = luckPermsAPI ?: error("No LuckPerms plugin service found.")
                val user = when (unique) {
                    is Player -> api.getPlayerAdapter(Player::class.java).getUser(unique)
                    else -> {
                        val player = Bukkit.getPlayerExact(unique.toString()) ?: frame.playerOrNull()?.toBukkit()
                        api.getPlayerAdapter(Player::class.java).getUser(player ?: error("No LuckPerms player selected."))
                    }
                }

                user.data().clear(NodeType.META.predicate { it.metaKey.equals(key, true) })

                val node = MetaNode.builder(key, value.toString()).build()
                if (value != null) {
                    user.data().add(node)
                } else {
                    user.data().remove(node)
                }
                api.userManager.saveUser(user)
            }
            "aboleth", "abo" -> {
                val uuid = when (unique) {
                    is Player -> unique.uniqueId
                    is ProxyPlayer -> unique.uniqueId
                    is String -> Bukkit.getPlayerExact(unique)?.uniqueId
                    else -> null
                }

                if (value != null) {
                    AbolethUtils.set(uuid ?: AbolethUtils.getServerUUID(), key, value)
                } else {
                    AbolethUtils.remove(uuid ?: AbolethUtils.getServerUUID(), key)
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
}