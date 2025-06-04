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

    override val isInitialized
        get() = source.isInitialized

    override fun getValue(): T {
        return try {
            source.getValue()
        } catch (e: ConfigFieldReadException) {
            exceptionally.apply(e.cause as Exception)
        } catch (e: Exception) {
            exceptionally.apply(e)
        }
    }

    override fun update() {
        source.update()
    }

}