package top.lanscarlos.vulpecula.config

import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2023-08-25 00:31
 */
class DefaultDynamicSection<T>(
    override val parent: DynamicConfig,
    override val path: String,
    val transfer: Function<Any?, T>
) : DynamicSection<T> {

    var value: T? = null
    var isInitialized = false

    @Suppress("UNCHECKED_CAST")
    override fun getValue(): T {
        if (!isInitialized) {
            value = transfer.apply(parent[path])
            isInitialized = true
        }
        return value as T
    }

    override fun update() {
        value = transfer.apply(parent[path])
        isInitialized = true
    }
}