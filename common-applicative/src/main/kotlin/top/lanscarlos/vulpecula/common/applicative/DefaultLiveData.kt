package top.lanscarlos.vulpecula.common.applicative

import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 17:25
 */
class DefaultLiveData<T>(private var source: Any?, val applicative: Applicative<T>) : LiveData<T> {

    /**
     * 缓存值
     * */
    private var value: T? = null

    /**
     * 是否已初始化
     * */
    private var isInitialized = false

    override fun getValue(): T {
        return getValueOrNull() ?: error("Value is null.")
    }

    override fun getValueOrNull(): T? {
        if (!isInitialized) {
            // 初始化
            value = applicative.convert(source)
            isInitialized = true
        }
        return value
    }

    override fun update(source: Any?) {
        this.source = source
        isInitialized = false
    }

    override fun getValue(parent: Any?, property: KProperty<*>): T {
        return getValue()
    }

}