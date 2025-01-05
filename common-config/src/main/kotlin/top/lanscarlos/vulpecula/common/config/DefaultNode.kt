package top.lanscarlos.vulpecula.common.config

import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2024-12-16 01:48
 */
class DefaultNode<T>(
    val section: ConfigSection,
    override val path: String,
    private val transfer: Function<Any?, T>
) : ConfigNode<T> {

    private var value: T? = null

    private var isInitialized = false

    @Suppress("UNCHECKED_CAST")
    override fun getValue(): T {
        if (!isInitialized) {
            update()
            isInitialized = true
        }
        return value as T
    }

    override fun update() {
        value = transfer.apply(section[path])
    }

}