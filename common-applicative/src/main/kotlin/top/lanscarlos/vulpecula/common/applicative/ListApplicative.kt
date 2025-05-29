package top.lanscarlos.vulpecula.common.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 11:15
 */
object ListApplicative : AbstractApplicative<List<*>>(List::class.java) {

    override fun convert(instance: Any): List<*> {
        return when (instance) {
            is Array<*> -> instance.toList()
            is Collection<*> -> instance.toList()
            is Map<*, *> -> instance.toList()
            else -> listOf(instance)
        }
    }

    override fun readProperty(instance: List<Any?>, key: String): Any? {
        return when (key) {
            "size" -> instance.size
            "isEmpty", "empty" -> instance.isEmpty()
            "isNotEmpty", "notEmpty" -> instance.isNotEmpty()
            "first" -> instance.firstOrNull()
            "last" -> instance.lastOrNull()
            "random" -> instance.randomOrNull()
            else -> {
                val index = key.toIntOrNull() ?: errorGetPropertyNotSupported(instance, key)
                instance.getOrNull(index)
            }
        }
    }

    override fun writeProperty(instance: List<Any?>, key: String, value: Any?) {
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
                    instance[0] = value
                }
                "last" -> {
                    instance[instance.lastIndex] = value
                }
                "random" -> {
                    instance[(instance.indices).random()] = value
                }
                else -> {
                    val index = key.toIntOrNull() ?: errorBySetPropertyNotSupported(instance, key)
                    instance[index] = value
                }
            }
        } else {
            errorBySetPropertyNotSupported(instance, key)
        }
    }
}