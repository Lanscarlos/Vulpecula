package top.lanscarlos.vulpecula.common.config

import top.lanscarlos.vulpecula.common.applicative.Applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2024-12-16 01:48
 */
class DefaultSection<T>(override val config: DynamicConfig, override val path: String, val applicative: Applicative<T>, val defaultValue: T?) : DynamicSection<T> {

    private var value: T? = null

    var isInitialized = false

    @Suppress("UNCHECKED_CAST")
    override fun getValue(): T {
        if (!isInitialized) {
            val rawValue = config.get(path)
            if (rawValue == null) {
                value = defaultValue ?: error("DefaultSection#getValue >> Property $path not found")
                return value as T
            }
            value = if (defaultValue != null) {
                applicative.apply(rawValue, defaultValue)
            } else {
                applicative.apply(rawValue)
            }
            isInitialized = true
        }
        return value as T
    }

    override fun update() {
        val rawValue = config.get(path) ?: return
        value = if (defaultValue != null) {
            applicative.apply(rawValue, defaultValue)
        } else {
            applicative.apply(rawValue)
        }
    }

}