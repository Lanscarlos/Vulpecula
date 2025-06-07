package top.lanscarlos.vulpecula.common.config

import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldReadException
import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldNotFoundException
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

    /**
     * 缓存值
     * */
    private var value: R? = null

    init {
        update()
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(): R {
        return value as R
    }

    override fun update() {
        this.source.update()
        try {
            value = transfer.apply(source.getValue())
        } catch (_: NullPointerException) {
            // 缺少必要字段
            throw ConfigFieldNotFoundException(id)
        } catch (e: ConfigFieldReadException) {
            throw e
        } catch (e: Exception) {
            throw ConfigFieldReadException(id, e)
        }
    }

}