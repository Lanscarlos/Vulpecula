package top.lanscarlos.vulpecula.module.action.memory

import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.memory
 *
 * @author Lanscarlos
 * @since 2025/6/30
 */

const val DEFAULT_NAMESPACE = "@"
const val DEFAULT_STORAGE = "vulpecula"

@BacikalParser("memory.get")
object ActionMemoryGet : ClassActionResolver {

    fun resolve(
        key: String,
        @Additional(["namespace"]) namespace: String = DEFAULT_NAMESPACE,
        @Additional(["storage"]) storage: String = DEFAULT_STORAGE,
    ): Any? {
        val storage = getStorage(storage)
        return storage.get(key, namespace)
    }

}

@BacikalParser("memory.set")
object ActionMemorySet : ClassActionResolver {

    fun resolve(
        key: String,
        value: Any?,
        @Additional(["namespace"]) namespace: String = DEFAULT_NAMESPACE,
        @Additional(["storage"]) storage: String = DEFAULT_STORAGE,
    ): Any? {
        val storage = getStorage(storage)
        if (value == null) {
            return storage.remove(key, namespace)
        }
        return storage.set(key, value, namespace)
    }

}

@BacikalParser("memory.remove")
object ActionMemoryRemove : ClassActionResolver {

    fun resolve(
        key: String,
        @Additional(["namespace"]) namespace: String = DEFAULT_NAMESPACE,
        @Additional(["storage"]) storage: String = DEFAULT_STORAGE,
    ): Any? {
        val storage = getStorage(storage)
        return storage.remove(key, namespace)
    }

}

private fun getStorage(storage: String): MemoryStorage {
    return when (storage) {
        "vulpecula" -> VulpeculaMemoryStorage
        else -> error(asLang("module-action-memory-exception-invalid-storage", storage))
    }
}