package top.lanscarlos.vulpecula.bacikal

import taboolib.common.OpenResult
import taboolib.module.kether.ScriptProperty
import taboolib.module.kether.action.ActionProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-03-19 22:17
 */
abstract class BacikalScriptProperty<T : Any>(
    id: String
) : ScriptProperty<T>("vulpecula.$id.operator") {

    abstract fun readProperty(instance: T, key: String): OpenResult

    abstract fun writeProperty(instance: T, key: String, value: Any?): OpenResult

    override fun read(instance: T, key: String): OpenResult {
        return if (key.contains('.')) {
            val path = key.split('.')
            // 连续读取
            readPropertyDeep(instance, path)
        } else {
            readProperty(instance, key)
        }
    }

    override fun write(instance: T, key: String, value: Any?): OpenResult {
        return if (key.contains('.')) {
            val path = key.split('.')
            // 连续读取写入
            writePropertyDeep(instance, path, value)
        } else {
            writeProperty(instance, key, value)
        }
    }

    /**
     * 连续读取
     * */
    protected fun readPropertyDeep(instance: T, path: List<String>): OpenResult {
        return when {
            path.isEmpty() -> OpenResult.failed()
            path.size == 1 -> readProperty(instance, path.first())
            path.size == 2 -> {
                val cache = readProperty(instance, path.first())
                if (cache.isFailed) return OpenResult.failed()
                readGenericProperty(cache.get() ?: return OpenResult.successful(), path.last())
            }
            else -> {
                var cache = readProperty(instance, path.first())
                if (cache.isFailed) return OpenResult.failed()

                for (i in 1 until path.lastIndex) {
                    // 遍历除最后一个外所有节点
                    cache = readGenericProperty(cache.get() ?: return OpenResult.successful(), path[i])
                }

                if (cache.isFailed) return OpenResult.failed()
                readGenericProperty(cache.get() ?: return OpenResult.successful(), path.last())
            }
        }
    }

    /**
     * 连续读取写入
     * */
    protected fun writePropertyDeep(instance: T, path: List<String>, value: Any?): OpenResult {
        return when {
            path.isEmpty() -> OpenResult.failed()
            path.size == 1 -> writeProperty(instance, path.first(), value)
            path.size == 2 -> {
                val cache = readProperty(instance, path.first())
                if (cache.isFailed) return OpenResult.failed()
                writeGenericProperty(cache.get() ?: return OpenResult.successful(), path.last(), value)
            }
            else -> {
                var cache = readProperty(instance, path.first())
                if (cache.isFailed) return OpenResult.failed()

                for (i in 1 until path.lastIndex) {
                    // 遍历除最后一个外所有节点
                    cache = readGenericProperty(cache.get() ?: return OpenResult.successful(), path[i])
                }

                if (cache.isFailed) return OpenResult.failed()
                writeGenericProperty(cache.get() ?: return OpenResult.successful(), path.last(), value)
            }
        }
    }

    /**
     * 读取泛型属性
     * */
    protected fun <R: Any> readGenericProperty(instance: R, key: String): OpenResult {
        BacikalRegistry.getScriptProperties(instance).forEach {
            // 这里不再使用泛型读取，防止套娃
            val result = it.readProperty(instance, key)
            if (result.isSuccessful) return result
        }

        // 使用原版读取
        ActionProperty.getScriptProperty(instance).filterIsInstance<ScriptProperty<Any?>>().forEach {
            // 排除自己，防止无限调用
            if (this.id == it.id) return@forEach

            // 这里不再使用泛型写入，防止套娃
            val result = it.read(instance, key)
            if (result.isSuccessful) return result
        }
        return OpenResult.failed()
    }

    /**
     * 写入泛型属性
     * */
    protected fun <R: Any> writeGenericProperty(instance: R, key: String, value: Any?): OpenResult {
        BacikalRegistry.getScriptProperties(instance).forEach {
            // 这里不再使用泛型写入，防止套娃
            val result = it.writeProperty(instance, key, value)
            if (result.isSuccessful) return result
        }

        // 使用原版写入
        ActionProperty.getScriptProperty(instance).filterIsInstance<ScriptProperty<Any?>>().forEach {
            // 排除自己，防止无限调用
            if (this.id == it.id) return@forEach

            // 这里不再使用泛型写入，防止套娃
            val result = it.write(instance, key, value)
            if (result.isSuccessful) return result
        }
        return OpenResult.failed()
    }

    fun OpenResult.get(): Any? {
        return if (this.isSuccessful) this.value else null
    }
}