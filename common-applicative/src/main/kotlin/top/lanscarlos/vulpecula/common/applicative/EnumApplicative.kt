package top.lanscarlos.vulpecula.common.applicative

import org.bukkit.GameMode

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2025/9/9
 */
class EnumApplicative<T>(clazz: Class<T>) : AbstractApplicative<T>(clazz) {

    val enumMapping = clazz.enumConstants?.associateBy { (it as Enum<*>).name.uppercase() } ?: error("Invalid enum class: ${clazz.name}")

    override fun convertOrThrow(instance: Any): T {
        if (instance !is String) {
            error("Value is not a string type. Actual value: $instance")
        }
        return enumMapping[instance.uppercase()] ?: error("Invalid enum value: $instance")
    }

    override fun readProperty(instance: T, key: String): Any {
        errorGetPropertyNotSupported(instance, key)
    }

    override fun writeProperty(instance: T, key: String, value: Any?) {
        errorBySetPropertyNotSupported(instance, key)
    }

}