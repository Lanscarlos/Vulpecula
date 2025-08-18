package top.lanscarlos.vulpecula.module.bacikal.property

import taboolib.common.OpenResult
import taboolib.module.kether.ScriptProperty
import taboolib.module.kether.action.ActionProperty
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry
import top.lanscarlos.vulpecula.module.bacikal.exception.NoSuchPropertyException
import kotlin.collections.set

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property
 *
 * @author Lanscarlos
 * @since 2025/8/3
 */
class BacikalPropertyResolver<T: Any>(
    id: String,
    val bind: Class<T>
) : ScriptProperty<T>("vulpecula.$id.operator") {

    /**
     * 关联的 BacikalProperty
     * */
    private val relatedProperties: List<BacikalProperty<in T>> = getRelatedProperties(bind)

    /**
     * 缓存的关联属性, 获取属性过程中，如果调用了关联 BacikalProperty, 那么就记录缓存
     * */
    private val relatedBacikalCache: HashMap<String, BacikalProperty<in T>> = linkedMapOf()

    /**
     * 缓存的关联属性, 获取属性过程中，如果调用了关联 ScriptProperty, 那么就记录缓存
     * */
    private val relatedKetherCache: HashMap<String, ScriptProperty<in T>> = linkedMapOf()

    override fun read(instance: T, key: String): OpenResult {
        return try {
            val value = if (key.contains('.')) readPropertyDeep(instance, key) else readProperty(instance, key)
            OpenResult.successful(value)
        } catch (_: NoSuchPropertyException) {
            OpenResult.failed()
        }
    }

    override fun write(instance: T, key: String, value: Any?): OpenResult {
        return try {
            if (key.contains('.')) {
                writeProperty(instance, key, value)
            } else {
                writePropertyDeep(instance, key, value)
            }
            OpenResult.successful()
        } catch (e: Exception) {
            e.printStackTrace()
            OpenResult.failed()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun readProperty(instance: T, key: String): Any? {
        if (relatedBacikalCache.containsKey(key)) {
            // 缓存中存在
            return relatedBacikalCache[key]!!.readProperty(instance, key)
        }
        if (relatedKetherCache.containsKey(key)) {
            // 缓存中存在
            return relatedKetherCache[key]!!.read(instance, key)
        }
        for (property in relatedProperties) {
            try {
                val result = property.readProperty(instance, key)
                relatedBacikalCache[key] = property
                return result
            } catch (_: NoSuchPropertyException) {
            } catch (ex: Exception) {
                throw ex
            }
        }
        val ketherProperty = ActionProperty.getScriptProperty(instance)
            .filter { it !is BacikalPropertyResolver<*> }
            .map { it as ScriptProperty<Any> }
        for (property in ketherProperty) {
            val result = property.read(instance, key)
            if (result.isSuccessful) {
                relatedKetherCache[key] = property
                return result.value
            }
        }
        throw NoSuchPropertyException(bind, key)
    }

    @Suppress("UNCHECKED_CAST")
    private fun writeProperty(instance: T, key: String, value: Any?) {
        if (relatedBacikalCache.containsKey(key)) {
            // 缓存中存在
            relatedBacikalCache[key]!!.writeProperty(instance, key, value)
            return
        }
        if (relatedKetherCache.containsKey(key)) {
            // 缓存中存在
            relatedKetherCache[key]!!.write(instance, key, value)
            return
        }
        for (property in relatedProperties) {
            try {
                property.writeProperty(instance, key, value)
            } catch (_: NoSuchPropertyException) {
            } catch (ex: Exception) {
                throw ex
            }
        }
        val ketherProperty = ActionProperty.getScriptProperty(instance)
            .filter { it !is BacikalPropertyResolver<*> }
            .map { it as ScriptProperty<Any> }
        for (property in ketherProperty) {
            val result = property.write(instance, key, value)
            if (result.isSuccessful) {
                relatedKetherCache[key] = property
                return
            }
        }
        throw NoSuchPropertyException(bind, key)
    }

    @JvmName("readPropertyGeneric")
    private fun <R: Any> readProperty(instance: R, key: String): Any? {
        val clazz = instance.javaClass
        for (property in getRelatedProperties(clazz)) {
            try {
                return property.readProperty(instance, key)
            } catch (_: NoSuchPropertyException) {
            } catch (ex: Exception) {
                throw ex
            }
        }
        throw NoSuchPropertyException(clazz, key)
    }

    @JvmName("writePropertyGeneric")
    private fun <R: Any> writeProperty(instance: R, key: String, value: Any?) {
        val clazz = instance.javaClass
        for (property in getRelatedProperties(clazz)) {
            try {
                property.writeProperty(instance, key, value)
            } catch (_: NoSuchPropertyException) {
            } catch (ex: Exception) {
                throw ex
            }
        }
        throw NoSuchPropertyException(clazz, key)
    }

    private fun readPropertyDeep(instance: T, key: String): Any? {
        val paths = key.split('.')
        require(paths.size >= 2) { "Invalid path: $key at ${instance.javaClass.name}" }

        var index = 0
        var cache: Any? = readProperty(instance, paths[index])
        while (++index < paths.size) {
            if (cache == null) {
                // 中间属性为空
                if (paths[index].last() == '?') {
                    // 安全返回可空类型
                    return null
                }
                val name = paths.subList(0, index + 1).joinToString(".")
                error("${instance.javaClass.name}[$key] read failed. ${instance.javaClass.name }}[$name] is null.")
            }
            cache = readProperty(cache, paths[index].removeSuffix("?"))
        }
        return cache
    }

    private fun writePropertyDeep(instance: T, key: String, value: Any?) {
        val parentPath = key.substringBeforeLast('.')
        val cache: Any? = readPropertyDeep(instance, parentPath)
        if (cache == null) {
            // 中间属性为空
            if (parentPath.last() == '?') {
                // 安全返回可空类型
                return
            }
            error("${instance.javaClass.name}[$key] write failed. ${instance.javaClass.name }}[$parentPath] is null.")
        }
        writeProperty(cache, key.substringAfterLast('.').removeSuffix("?"), value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <R : Any> getRelatedProperties(clazz: Class<R>): List<BacikalProperty<in R>> {
        return BacikalRegistry.getPropertyEntries()
            .filter {
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
                it.second as BacikalProperty<in R>
            }
    }

}