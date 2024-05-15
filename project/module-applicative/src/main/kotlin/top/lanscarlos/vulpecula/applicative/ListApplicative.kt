package top.lanscarlos.vulpecula.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 11:15
 */
abstract class ListApplicative<T> : AbstractApplicative<List<T>>() {

    abstract fun mapping(instance: Any?): T

    override fun transfer(instance: Any, def: List<T>?): List<T>? {
        return when (instance) {
            is Array<*> -> instance.map(::mapping)
            is Collection<*> -> instance.map(::mapping)
            is Map<*, *> -> instance.map(::mapping)
            else -> def
        }
    }

    override fun readProperty(instance: List<T>, key: String): Any? {
        return when (key) {
            "size" -> instance.size
            "isEmpty", "empty" -> instance.isEmpty()
            "isNotEmpty", "notEmpty" -> instance.isNotEmpty()
            "first" -> instance.firstOrNull()
            "last" -> instance.lastOrNull()
            "random" -> instance.randomOrNull()
            else -> {
                val index = key.toIntOrNull() ?: failedByGetPropertyNotSupported(instance, key)
                instance.getOrNull(index)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun writeProperty(instance: List<T>, key: String, value: Any?) {
        if (instance is MutableList) {
            when (key) {
                "isEmpty", "empty" -> {
                    if (value.applicativeBoolean()) {
                        instance.clear()
                    }
                }
                "isNotEmpty", "notEmpty" -> {
                    if (!value.applicativeBoolean()) {
                        instance.clear()
                    }
                }
                "first" -> {
                    instance[0] = value as? T ?: failedByInvalidValue(instance, key, value)
                }
                "last" -> {
                    instance[instance.lastIndex] = value as? T ?: failedByInvalidValue(instance, key, value)
                }
                "random" -> {
                    instance[(instance.indices).random()] = value as? T ?: failedByInvalidValue(instance, key, value)
                }
                else -> {
                    val index = key.toIntOrNull() ?: failedBySetPropertyNotSupported(instance, key)
                    instance[index] = value as? T ?: failedByInvalidValue(instance, key, value)
                }
            }
        } else {
            failedBySetPropertyNotSupported(instance, key)
        }
    }
}