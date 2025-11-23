package top.lanscarlos.vulpecula.common.config

import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldReadException
import java.util.function.Consumer
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025/5/28 11:05
 */
class ExceptionalLiveData<T>(private val source: LiveData<T>, private val exceptionally: Function<Exception, T>) : LiveData<T> {

    override val id: String
        get() = source.id

    private var onUpdate: Consumer<T>? = null

    private var value: T? = null

    init {
        source.onUpdate(::update)
        update(source.getValue())
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(): T {
        return value as T
    }

    override fun onUpdate(func: Consumer<T>) {
        this.onUpdate = func
    }

    fun update(value: T) {
        try {
            this.value = value
        } catch (e: ConfigFieldReadException) {
            exceptionally.apply(e.cause as Exception)
        } catch (e: Exception) {
            exceptionally.apply(e)
        }
    }

}