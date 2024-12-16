package top.lanscarlos.vulpecula.common.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 17:25
 */
class DefaultLiveData<T>(val source: Any, val applicative: Applicative<T>) : MutableLiveData<T> {

    /**
     * 缓存值
     * */
    private var value: T? = null

    /**
     * 是否已初始化
     * */
    private var isInitialized = false

    override fun getValue(): T? {
        if (!isInitialized) {
            value = applicative.apply(source)
        }
        return value
    }

    override fun getValue(def: T): T {
        if (!isInitialized) {
            value = applicative.apply(source, def)
        }
        return value ?: def
    }

    override fun get(key: String): Any? {
        val instance = getValue() ?: return null
        return applicative.getProperty(instance, key)
    }

    override fun set(key: String, value: Any?) {
        val instance = getValue() ?: return
        applicative.setProperty(instance, key, value)
    }
}