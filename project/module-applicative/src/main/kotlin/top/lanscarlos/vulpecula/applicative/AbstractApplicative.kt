package top.lanscarlos.vulpecula.applicative

import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 13:57
 */
abstract class AbstractApplicative<T>(val source: Any) : Applicative<T> {

    private var value: T? = null
    private var isInitialized = false

    abstract fun transfer(source: Any, def: T?): T?

    override fun getValue(): T? {
        if (!isInitialized) {
            value = transfer(source, null)
        }
        return value
    }

    override fun getValue(def: T): T {
        if (!isInitialized) {
            value = transfer(source, def)
        }
        return value ?: def
    }

    override fun getValue(parent: Any?, property: KProperty<*>): T? {
        return getValue()
    }
}
