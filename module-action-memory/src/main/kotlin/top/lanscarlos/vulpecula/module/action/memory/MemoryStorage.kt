package top.lanscarlos.vulpecula.module.action.memory

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.memory
 *
 * 记忆存储容器
 *
 * @author Lanscarlos
 * @since 2025/6/30
 */
interface MemoryStorage {

    /**
     * 获取数据
     * */
    fun get(key: String, namespace: String): Any?

    /**
     * 设置数据
     * */
    fun set(key: String, value: Any, namespace: String): Boolean

    /**
     * 移除数据
     * */
    fun remove(key: String, namespace: String): Any?

}