package top.lanscarlos.vulpecula.module.property.${module}

import ${import}
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalProperty
import top.lanscarlos.vulpecula.common.applicative.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property
 *
 * @author Lanscarlos
 * @since ${time}
 */
@Property(bind = ${target}::class)
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
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}