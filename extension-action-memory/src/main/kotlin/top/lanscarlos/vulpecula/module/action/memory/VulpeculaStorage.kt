package top.lanscarlos.vulpecula.module.action.memory

import org.bukkit.entity.Entity
import taboolib.common.platform.function.console
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.memory
 *
 * @author Lanscarlos
 * @since 2025/6/30
 */
object VulpeculaStorage : MemoryStorage {

    private val data: HashMap<String, HashMap<String, Any>> = hashMapOf()

    override fun get(key: String, owner: Any): Any? {
        return get(key, getNamespace(owner))
    }

    override fun set(key: String, value: Any, owner: Any): Boolean {
        return set(key, value, getNamespace(owner))
    }

    override fun remove(key: String, owner: Any): Any? {
        return remove(key, getNamespace(owner))
    }

    fun get(key: String, namespace: String): Any? {
        val storage = data.computeIfAbsent(namespace) { hashMapOf() }
        return storage[key]
    }

    fun set(key: String, value: Any, namespace: String): Boolean {
        val storage = data.computeIfAbsent(namespace) { hashMapOf() }
        val exist = storage.contains(key)
        storage[key] = value
        return exist
    }

    fun remove(key: String, namespace: String): Any? {
        val storage = data.computeIfAbsent(namespace) { hashMapOf() }
        return storage.remove(key)
    }

    private fun getNamespace(owner: Any): String {
        return when (owner) {
            is String -> owner
            is Entity -> owner.uniqueId.toString()
            else -> error(Lang.ACTION_MEMORY_EXCEPTION_UNSUPPORTED_OWNER_TYPE.asText(console(), "Vulpecula", owner::class.java.name))
        }
    }

}