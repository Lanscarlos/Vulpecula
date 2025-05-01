package top.lanscarlos.vulpecula.common.livedata

import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.livedata
 *
 * @author Lanscarlos
 * @since 2025-03-10 19:12
 */
class ProxyLiveData<T, R>(val source: LiveData<T>, val transfer: Function<T, R>) : LiveData<R> {

    /**
     * 缓存值
     * */
    private var value: R? = null

    /**
     * 是否已初始化
     * */
    override val isInitialized
        get() = source.isInitialized

    @Suppress("UNCHECKED_CAST")
    override fun getValue(): R {
        if (!isInitialized) {
            // 初始化
            value = transfer.apply(source.getValue())
        }
        return value as R
    }

    override fun getValueOrNull(): R? {
        if (!isInitialized) {
            // 初始化
            value = source.getValueOrNull()?.let(transfer::apply)
        }
        return value
    }

    override fun update() {
        this.source.update()
    }

}