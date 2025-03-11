package top.lanscarlos.vulpecula.common.livedata

import java.util.function.Function
import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.livedata
 *
 * @author Lanscarlos
 * @since 2025-03-10 19:12
 */
class ProxyLiveData<T, R>(val source: LiveData<T>, val transfer: Function<T, R>) : LiveData<R> {

    override fun getValue(): R {
        return transfer.apply(source.getValue())
    }

    override fun getValueOrNull(): R? {
        return source.getValueOrNull()?.let(transfer::apply)
    }

    override fun getValue(parent: Any?, property: KProperty<*>): R {
        return getValue()
    }

    override fun update(source: Any?) {
        this.source.update(source)
    }

}