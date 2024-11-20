package top.lanscarlos.vulpecula.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 13:57
 */
abstract class AbstractApplicative<T: Any> : Applicative<T> {

    /**
     * 转换规则
     * */
    abstract fun transfer(instance: Any, def: T?): T?

    /**
     * 读取属性
     *
     * @throws IllegalStateException 如果属性不存在
     * */
    abstract fun readProperty(instance: T, key: String): Any?

    /**
     * 写入属性
     *
     * @throws IllegalStateException 如果属性不存在
     * */
    abstract fun writeProperty(instance: T, key: String, value: Any?)

    override fun apply(instance: Any): T? {
        return transfer(instance, null)
    }

    override fun apply(instance: Any, def: T): T {
        return transfer(instance, def) ?: def
    }

    override fun applyUnsafe(instance: Any): T {
        return apply(instance) ?: error("AbstractApplicative#applyUnsafe >> Cannot apply ${instance.javaClass.name} to ${this::class.java.name}.")
    }

    override fun accept(instance: Any): LiveData<T> {
        return DefaultLiveData(instance, this)
    }

    override fun getProperty(instance: T, key: String, strict: Boolean): Any? {
        if (!key.contains('.')) {
            return try {
                readProperty(instance, key.toCamelCase())
            } catch (ignored: Exception) {
                readGenericProperty(instance, key.toCamelCase(), strict)
            }
        }
        val path = key.toCamelCase().split('.')
        if (path.size < 2) {
            error("Invalid path: $key at ${instance::class.java.name}")
        }

        var index = 0
        var cache: Any? = try {
            readProperty(instance, key.toCamelCase())
        } catch (ignored: Exception) {
            readGenericProperty(instance, path[index], strict)
        }
        while (++index < path.size) {
            if (cache == null) {
                if (strict) {
                    val name = buildString { for (i in 0 until index) append("${path[i]}.") }
                    error("${instance::class.java.name}[$key] read failed. ${instance::class.java.simpleName}[$name] is null.")
                }
                return null
            }
            cache = readGenericProperty(cache, path[index], strict)
        }
        return cache
    }

    override fun setProperty(instance: T, key: String, value: Any?, strict: Boolean) {
        if (!key.contains('.')) {
            try {
                writeProperty(instance, key.toCamelCase(), value)
            } catch (ignored: Exception) {
                writeGenericProperty(instance, key.toCamelCase(), value, strict)
            }
        }
        val path = key.toCamelCase().split('.')
        if (path.size < 2) {
            error("Invalid path: $key at ${instance::class.java.name}")
        }

        var index = 0
        var cache: Any? = try {
            readProperty(instance, key.toCamelCase())
        } catch (ignored: Exception) {
            readGenericProperty(instance, path[index], strict)
        }
        while (++index < path.size - 1) {
            if (cache == null) {
                if (strict) {
                    val name = buildString { for (i in 0 until index) append("${path[i]}.") }
                    error("${instance::class.java.name}[$key] read failed. ${instance::class.java.simpleName}[$name] is null.")
                }
                return
            }
            cache = readGenericProperty(cache, path[index], strict)
        }

        if (cache == null) {
            if (strict) {
                val name = buildString { for (i in 0 until index) append("${path[i]}.") }
                error("${instance::class.java.name}[$key] read failed. ${instance::class.java.simpleName}[$name] is null.")
            }
            return
        }
        writeGenericProperty(cache, path.last(), value, strict)
    }

    fun <R: Any> readGenericProperty(instance: R, key: String, strict: Boolean): Any? {
        val applicatives = ApplicativeRegistry.getRelatedApplicative(instance::class.java)
        for (applicative in applicatives.filterIsInstance<Applicative<Any>>()) {
            try {
                val property = if (applicative is AbstractApplicative) {
                    // 不使用泛型读取，防止套娃
                    applicative.readProperty(instance, key)
                } else {
                    applicative.getProperty(instance, key, strict)
                }
                return property
            } catch (_: Exception) {
            }
        }
        // 未找到对应的属性
        if (strict) {
            error("${instance.javaClass.name}[$key] not supported yet.")
        }
        return null
    }

    fun <R: Any> writeGenericProperty(instance: R, key: String, value: Any?, strict: Boolean): Any? {
        val applicatives = ApplicativeRegistry.getRelatedApplicative(instance::class.java)
        for (applicative in applicatives.filterIsInstance<Applicative<Any>>()) {
            try {
                val property = if (applicative is AbstractApplicative) {
                    // 不使用泛型写入，防止套娃
                    applicative.writeProperty(instance, key, value)
                } else {
                    applicative.setProperty(instance, key, value, strict)
                }
                return property
            } catch (_: Exception) {
            }
        }
        // 未找到对应的属性
        if (strict) {
            error("${instance.javaClass.name}[$key] not supported yet.")
        }
        return null
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
