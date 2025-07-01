package top.lanscarlos.vulpecula.module.action.memory

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.memory
 *
 * @author Lanscarlos
 * @since 2025/6/30
 */
object VulpeculaMemoryStorage : MemoryStorage {

    private val data: HashMap<String, HashMap<String, Any>> = hashMapOf()

    override fun get(key: String, namespace: String): Any? {
        val storage = data.computeIfAbsent(namespace) { hashMapOf() }
        return storage[key]
    }

    override fun set(key: String, value: Any, namespace: String): Boolean {
        val storage = data.computeIfAbsent(namespace) { hashMapOf() }
        val exist = storage.contains(key)
        storage[key] = value
        return exist
    }

    override fun remove(key: String, namespace: String): Any? {
        val storage = data.computeIfAbsent(namespace) { hashMapOf() }
        return storage.remove(key)
    }

}