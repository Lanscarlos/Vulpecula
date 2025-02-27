package top.lanscarlos.vulpecula.common.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2025-02-27 17:12
 */
class RelatedApplicative<T: Any>(val converter: Applicative<T>, val relatedProperties: List<Applicative<in T>>) : Applicative<T> {

    init {
        if (relatedProperties.isEmpty()) {
            error("RelatedApplicative#init >> relatedProperties can not be empty.")
        }
    }

    override fun apply(instance: Any?): T? {
        return converter.apply(instance)
    }

    override fun applyUnsafe(instance: Any?): T {
        return converter.applyUnsafe(instance)
    }

    override fun accept(instance: Any): LiveData<T> {
        return converter.accept(instance)
    }

    override fun getProperty(instance: T, key: String, strict: Boolean, reflect: Boolean): Any? {
        for (property in relatedProperties) {
            try {
                return property.getProperty(instance, key, strict, reflect)
            } catch (_: Exception) {
            }
        }
        if (strict) {
            // 严格模式下未找到对应的属性, 抛出异常
            error("${instance.javaClass.name}[$key] not supported yet.")
        }
        return null
    }

    override fun setProperty(instance: T, key: String, value: Any?, strict: Boolean, reflect: Boolean) {
        for (property in relatedProperties) {
            try {
                property.setProperty(instance, key, value, strict, reflect)
                return
            } catch (_: Exception) {
            }
        }
        if (strict) {
            // 严格模式下未找到对应的属性, 抛出异常
            error("${instance.javaClass.name}[$key] not supported yet.")
        }
    }

}