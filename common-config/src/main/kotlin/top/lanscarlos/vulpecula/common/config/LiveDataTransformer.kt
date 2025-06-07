package top.lanscarlos.vulpecula.common.config

import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldReadException
import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldNotFoundException
import java.util.function.Consumer
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-03-10 19:12
 */
class LiveDataTransformer<T, R>(val source: LiveData<T>, val transfer: Function<T, R>) : LiveData<R> {

    override val id: String
        get() = source.id

    private var onUpdate: Consumer<R>? = null

    private var value: R? = null

    init {
        source.onUpdate(::update)
        update(source.getValue())
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(): R {
        return value as R
    }

    override fun onUpdate(func: Consumer<R>) {
        onUpdate = func
    }

    fun update(value: T) {
        try {
            this.value = transfer.apply(value)
            onUpdate?.accept(getValue())
        } catch (_: NullPointerException) {
            // 缺少必要字段
            throw ConfigFieldNotFoundException(id)
        } catch (e: ConfigFieldReadException) {
            if (e.field == id) {
                // 重复包装
                throw e
            }
            throw ConfigFieldReadException("$id.${e.field}", e)
        } catch (e: Exception) {
            throw ConfigFieldReadException(id, e)
        }
    }

}