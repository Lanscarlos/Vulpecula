package top.lanscarlos.vulpecula.common.livedata

import java.util.function.Function
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.livedata
 *
 * @author Lanscarlos
 * @since 2025-03-10 19:01
 */
class DefaultLiveData<T>(val source: Supplier<Any?>, val transformer: Function<Any?, T>) : LiveData<T> {

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
            value = transformer.apply(source.get())
            isInitialized = true
        }
        return value
    }

    override fun update() {
        isInitialized = false
    }

}