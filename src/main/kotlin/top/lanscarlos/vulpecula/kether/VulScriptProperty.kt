package top.lanscarlos.vulpecula.kether

import taboolib.common.OpenResult
import taboolib.module.kether.ScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether
 *
 * @author Lanscarlos
 * @since 2022-10-19 12:30
 */
abstract class VulScriptProperty<T : Any>(
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
                readGenericProperty(cache, path.last())
            }
            else -> {
                var cache: Any = readProperty(instance, path.first())
                for (i in 1 until path.lastIndex) {
                    // 遍历除最后一个外所有节点
                    cache = readGenericProperty(cache, path[i]).let {
                        if (it.isSuccessful) it.value else null
                    } ?: return OpenResult.failed()
                }
                readGenericProperty(cache, path.last())
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
                writeGenericProperty(cache, path.last(), value)
            }
            else -> {
                var cache: Any = readProperty(instance, path.first())
                for (i in 1 until path.lastIndex) {
                    // 遍历除最后一个外所有节点
                    cache = readGenericProperty(cache, path[i]).let {
                        if (it.isSuccessful) it.value else null
                    } ?: return OpenResult.failed()
                }
                writeGenericProperty(cache, path.last(), value)
            }
        }
    }

    /**
     * 读取泛型属性
     * */
    protected fun <R: Any> readGenericProperty(instance: R, key: String): OpenResult {
        KetherRegistry.getScriptProperties(instance).forEach {
            // 这里不再使用泛型读取，防止套娃
            val result = it.readProperty(instance, key)
            if (result.isSuccessful) return result
        }
        return OpenResult.failed()
    }

    /**
     * 写入泛型属性
     * */
    protected fun <R: Any> writeGenericProperty(instance: R, key: String, value: Any?): OpenResult {
        KetherRegistry.getScriptProperties(instance).forEach {
            // 这里不再使用泛型写入，防止套娃
            val result = it.writeProperty(instance, key, value)
            if (result.isSuccessful) return result
        }
        return OpenResult.failed()
    }
}