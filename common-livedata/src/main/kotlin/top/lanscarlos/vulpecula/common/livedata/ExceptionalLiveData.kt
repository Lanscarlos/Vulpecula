package top.lanscarlos.vulpecula.common.livedata

import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.livedata
 *
 * @author Lanscarlos
 * @since 2025/5/28 11:05
 */
class ExceptionalLiveData<T>(private val source: LiveData<T>, private val exceptionally: Function<Exception, T>) : LiveData<T> {

    override val isInitialized
        get() = source.isInitialized

    override fun getValue(): T {
        return try {
            source.getValue()
        } catch (e: Exception) {
            exceptionally.apply(e)
        }
    }

    override fun getValueOrNull(): T? {
        return try {
            source.getValueOrNull()
        } catch (e: Exception) {
            exceptionally.apply(e)
        }
    }

    override fun update() {
        source.update()
    }

}