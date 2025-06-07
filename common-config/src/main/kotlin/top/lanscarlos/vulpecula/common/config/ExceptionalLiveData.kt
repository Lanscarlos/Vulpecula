package top.lanscarlos.vulpecula.common.config

import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldReadException
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

    private var value: T? = null

    init {
        update()
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(): T {
        return value as T
    }

    override fun update() {
        source.update()
        try {
            value = source.getValue()
        } catch (e: ConfigFieldReadException) {
            exceptionally.apply(e.cause as Exception)
        } catch (e: Exception) {
            exceptionally.apply(e)
        }
    }

}