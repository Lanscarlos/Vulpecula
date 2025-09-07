package top.lanscarlos.vulpecula.module.bacikal.property

import ${import}
import top.lanscarlos.vulpecula.common.applicative.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property
 *
 * @author Lanscarlos
 * @since ${time}
 */
object ${name} : BacikalProperty<${target}> {

    @Suppress("DEPRECATION")
    override fun readProperty(instance: ${target}, key: String): Any? {
        return try {
            when(key) {
                ${getters}
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    @Suppress("DEPRECATION")
    override fun writeProperty(instance: ${target}, key: String, value: Any?) {
        try {
            when(key) {
                ${setters}
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}