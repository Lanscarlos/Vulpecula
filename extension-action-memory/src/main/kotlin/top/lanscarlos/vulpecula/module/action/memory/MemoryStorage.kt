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
     *
     * @param owner 数据所属对象
     * */
    fun get(key: String, owner: Any): Any?

    /**
     * 设置数据
     *
     * @param owner 数据所属对象
     * */
    fun set(key: String, value: Any, owner: Any): Boolean

    /**
     * 移除数据
     *
     * @param owner 数据所属对象
     * */
    fun remove(key: String, owner: Any): Any?

}