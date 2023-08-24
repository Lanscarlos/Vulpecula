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

    var innerValue: T? = null
    var isInitialized = false

    @Suppress("UNCHECKED_CAST")
    override fun getValue(): T {
        if (!isInitialized) {
            innerValue = transfer.apply(parent[path])
            isInitialized = true
        }
        return innerValue as T
    }

    override fun update() {
        innerValue = transfer.apply(parent[path])
        isInitialized = true
    }
}