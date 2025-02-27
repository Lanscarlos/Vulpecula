package top.lanscarlos.vulpecula.common.applicative

import taboolib.common.platform.function.warning

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 13:57
 */
abstract class AbstractApplicative<T: Any>(val clazz: Class<T>) : Applicative<T> {

    /**
     * 关联的 Applicative
     * */
    @Suppress("UNCHECKED_CAST")
    val relatedApplicatives: List<Applicative<in T>> = ApplicativeRegistry.registry.filter {
        it.value != this && it.key.isAssignableFrom(clazz)
    }.map {
        it.key to it.value
    }.sortedWith { a, b ->
        when {
            a.first == clazz -> -1
            b.first == clazz -> 1
            a.first.isAssignableFrom(b.first) -> 1
            else -> -1
        }
    }.map {
        it.second as Applicative<in T>
    }

    /**
     * 读取属性
     *
     * @throws IllegalStateException 如果属性不存在
     * */
    protected abstract fun readProperty(instance: T, key: String): Any?

    /**
     * 写入属性
     *
     * @throws IllegalStateException 如果属性不存在
     * */
    protected abstract fun writeProperty(instance: T, key: String, value: Any?)

    override fun applyUnsafe(instance: Any?): T {
        return apply(instance) ?: error("AbstractApplicative#applyUnsafe >> Cannot apply ${instance?.javaClass?.name} to ${this::class.java.name}.")
    }

    override fun accept(instance: Any): LiveData<T> {
        return DefaultLiveData(instance, this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getProperty(instance: T, key: String, strict: Boolean, reflect: Boolean): Any? {
        if (!key.contains('.')) {
            // 不含递归
            return readProperty(instance, key, strict, reflect)
        }

        // 含递归
        val path = key.toCamelCase().split('.')
        if (path.size < 2) {
            // 检查路径是否有效
            error("Invalid path: $key at ${instance::class.java.name}")
        }

        var index = 0
        var cache: Any? = readProperty(instance, path[index], strict, reflect)
        while (++index < path.size) {
            if (cache == null) {
                // 中间属性为空
                if (strict) {
                    val name = buildString { for (i in 0 until index) append("${path[i]}.") }
                    error("${instance::class.java.name}[$key] read failed. ${instance::class.java.simpleName}[$name] is null.")
                }
                return null
            }
            val applicative = ApplicativeRegistry.getApplicative(cache::class.java) as Applicative<Any>
            cache = applicative.getProperty(cache, path[index], strict, reflect)
        }
        return cache
    }

    @Suppress("UNCHECKED_CAST")
    override fun setProperty(instance: T, key: String, value: Any?, strict: Boolean, reflect: Boolean) {
        if (!key.contains('.')) {
            // 不含递归
            writeProperty(instance, key, value, strict, reflect)
            return
        }

        val path = key.toCamelCase().split('.')
        if (path.size < 2) {
            // 检查路径是否有效
            error("Invalid path: $key at ${instance::class.java.name}")
        }

        var index = 0
        var cache: Any? = readProperty(instance, path[index], strict, reflect)
        while (++index < path.size - 1) {
            if (cache == null) {
                // 中间属性为空
                if (strict) {
                    val name = buildString { for (i in 0 until index) append("${path[i]}.") }
                    error("${instance::class.java.name}[$key] read failed. ${instance::class.java.simpleName}[$name] is null.")
                }
                return
            }
            val applicative = ApplicativeRegistry.getApplicative(cache::class.java) as Applicative<Any>
            cache = applicative.getProperty(cache, path[index], strict, reflect)
        }

        if (cache == null) {
            // 中间属性为空
            if (strict) {
                val name = buildString { for (i in 0 until index) append("${path[i]}.") }
                error("${instance::class.java.name}[$key] read failed. ${instance::class.java.simpleName}[$name] is null.")
            }
            return
        }
        val applicative = ApplicativeRegistry.getApplicative(cache::class.java) as Applicative<Any>
        applicative.setProperty(cache, path.last(), value, strict, reflect)
    }

    /**
     * 读取属性
     *
     * @param key 属性名, 不支持递归
     * @throws IllegalStateException 如果属性不存在
     * */
    protected fun readProperty(instance: T, key: String, strict: Boolean, reflect: Boolean): Any? {
        try {
            return readProperty(instance, key.toCamelCase())
        } catch (ignored: Exception) {
            if (reflect) {
                // TODO 反射查找
            }

            // 当前类不存在该属性, 从关联父类检索
            for (applicative in relatedApplicatives) {
                try {
                    return applicative.getProperty(instance, key, strict, reflect)
                } catch (ignored: Exception) {
                }
            }

            // 关联父类未找到对应的属性
            if (strict) {
                // 严格模式下抛出异常
                failedByGetPropertyNotSupported(instance, key)
            }
        }
        return null
    }

    fun writeProperty(instance: T, key: String, value: Any?, strict: Boolean, reflect: Boolean) {
        try {
            writeProperty(instance, key.toCamelCase(), value)
        } catch (ignored: Exception) {
            if (reflect) {
                // TODO 反射查找
            }

            // 当前类不存在该属性, 从关联父类检索
            for (applicative in relatedApplicatives) {
                try {
                    applicative.setProperty(instance, key, value, strict, reflect)
                    return
                } catch (ignored: Exception) {
                }
            }

            // 关联父类未找到对应的属性
            if (strict) {
                // 严格模式下抛出异常
                failedBySetPropertyNotSupported(instance, key)
            }
            warning("Cannot set property in ${instance.javaClass.name}[$key]. Not supported yet.")
        }
    }

    private fun String.toCamelCase(): String {
        return buildString {
            var upper = false
            for (char in this@toCamelCase) {
                if (char == '-' || char == '_' || char == ' ') {
                    upper = true
                    continue
                }
                append(if (upper) char.uppercase() else char)
                upper = false
            }
        }
    }

    fun failedByGetPropertyNotSupported(instance: Any, key: String): Nothing {
        error("Cannot get property in ${instance.javaClass.name}[$key]. Not supported yet.")
    }

    fun failedBySetPropertyNotSupported(instance: Any, key: String): Nothing {
        error("Cannot set property in ${instance.javaClass.name}[$key]. Not supported yet.")
    }

    fun failedByInvalidValue(instance: Any, key: String, value: Any?): Nothing {
        error("Cannot set property in ${instance.javaClass.name}[$key]. Invalid value: $value::${value?.javaClass?.name}")
    }
}
