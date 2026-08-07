package top.lanscarlos.vulpecula.module.bacikal.property

import taboolib.common.OpenResult
import taboolib.module.kether.ScriptProperty
import taboolib.module.kether.action.ActionProperty
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry
import top.lanscarlos.vulpecula.module.bacikal.exception.NoSuchPropertyException
import top.lanscarlos.vulpecula.module.bacikal.extension.Extension
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
    val bind: Class<T>,
    val extension: Extension
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
                writePropertyDeep(instance, key, value)
            } else {
                writeProperty(instance, key, value)
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
                relatedBacikalCache[key] = property
                return
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
                return
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

        var cache: Any? = readProperty(instance, paths[0].removeSuffix("?"))
        for (index in 1 until paths.size) {
            if (cache == null) {
                // 上一段属性为空
                if (paths[index - 1].last() == '?') {
                    // 上一段已标记空安全, 返回 null
                    return null
                }
                val name = paths.subList(0, index).joinToString(".") { it.removeSuffix("?") }
                error("${instance.javaClass.name}[$key] read failed. ${instance.javaClass.name}[$name] is null.")
            }
            cache = readProperty(cache, paths[index].removeSuffix("?"))
        }
        return cache
    }

    private fun writePropertyDeep(instance: T, key: String, value: Any?) {
        val parentPath = key.substringBeforeLast('.')
        // 父路径可能只有一级, 此时不能走 readPropertyDeep, 后者要求路径至少两级
        val cache: Any? = if (parentPath.contains('.')) {
            readPropertyDeep(instance, parentPath)
        } else {
            readProperty(instance, parentPath.removeSuffix("?"))
        }
        if (cache == null) {
            // 父属性为空
            if (parentPath.last() == '?') {
                // 父属性已标记空安全, 跳过写入
                return
            }
            error("${instance.javaClass.name}[$key] write failed. ${instance.javaClass.name}[$parentPath] is null.")
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